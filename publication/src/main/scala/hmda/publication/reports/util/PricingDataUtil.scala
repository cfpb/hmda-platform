package hmda.publication.reports.util

import akka.NotUsed
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.publication.reports._
import hmda.util.SourceUtils

import scala.concurrent.Future
import scala.util.{ Success, Try }

object PricingDataUtil extends SourceUtils {

  def pricingData[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    for {
      noData <- pricingDisposition(lars, _.rateSpread == "NA", "No Reported Pricing Data")
      reported <- pricingDisposition(lars, pricingDataReported, "Reported Pricing Data")
      rs1_5 <- pricingDisposition(lars, rateSpreadBetween(1.5, 2), "1.50 - 1.99")
      rs2_0 <- pricingDisposition(lars, rateSpreadBetween(2, 2.5), "2.00 - 2.49")
      rs2_5 <- pricingDisposition(lars, rateSpreadBetween(2.5, 3), "2.50 - 2.99")
      rs3 <- pricingDisposition(lars, rateSpreadBetween(3, 4), "3.00 - 3.99")
      rs4 <- pricingDisposition(lars, rateSpreadBetween(4, 5), "4.00 - 4.99")
      rs5 <- pricingDisposition(lars, rateSpreadBetween(5, Int.MaxValue), "5 or more")
      mean <- reportedMean(lars)
      median <- reportedMedian(lars)
    } yield {
      s"""
         |[
         |    $noData,
         |    $reported,
         |    $rs1_5,
         |    $rs2_0,
         |    $rs2_5,
         |    $rs3,
         |    $rs4,
         |    $rs5,
         |    $mean,
         |    $median
         |]
     """.stripMargin
    }
  }

  private def rateSpreadBetween(lower: Double, upper: Double)(lar: LoanApplicationRegister): Boolean = {
    Try(lar.rateSpread.toDouble) match {
      case Success(value) => value >= lower && value < upper
      case _ => false
    }
  }

  def pricingDataReported(lar: LoanApplicationRegister): Boolean = {
    rateSpreadBetween(Int.MinValue, Int.MaxValue)(lar)
  }

  private def pricingDisposition[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed], filter: LoanApplicationRegister => Boolean, title: String): Future[String] = {
    val loansFiltered = larSource.filter(filter)
    val loanCountF = count(loansFiltered)
    val valueSumF = sum(loansFiltered, loanAmount)
    for {
      count <- loanCountF
      totalValue <- valueSumF
    } yield {
      s"""
         |{
         |    "pricing": "$title",
         |    "count": $count,
         |    "value": $totalValue
         |}
       """.stripMargin
    }
  }

  private def loanAmount(lar: LoanApplicationRegister): Int = lar.loan.amount
  def rateSpread(lar: LoanApplicationRegister): Double =
    Try(lar.rateSpread.toDouble).getOrElse(0)

  private def reportedMean[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val loansFiltered = lars.filter(pricingDataReported)
    val loanCountF = count(loansFiltered)
    val rateSpreadSumF = sumDouble(loansFiltered, rateSpread)
    for {
      count <- loanCountF
      totalRateSpread <- rateSpreadSumF
    } yield {
      val mean = if (count == 0) "\"\""
      else {
        val v = totalRateSpread / count
        BigDecimal(v).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
      }

      s"""
         |{
         |    "pricing": "Mean",
         |    "count": $mean,
         |    "value": "None"
         |}
       """.stripMargin
    }
  }

  private def reportedMedian[ec: EC, mat: MAT, as: AS](lars: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val rateSpreadsF: Future[Seq[Double]] =
      lars.filter(pricingDataReported)
        // .limit(MAX_SIZE)
        .map(lar => lar.rateSpread.toDouble)
        .runWith(Sink.seq)

    rateSpreadsF.map { seq =>
      val median = if (seq.isEmpty) "\"\"" else calculateMedian(seq)

      s"""
         |{
         |    "pricing": "Median",
         |    "count": $median,
         |    "value": "None"
         |}
       """.stripMargin
    }
  }

  def calculateMedian(seq: Seq[Double]): Double = {
    val (lowerHalf, upperHalf) = seq.sortWith(_ < _).splitAt(seq.size / 2)
    val median = if (seq.size % 2 == 0) (lowerHalf.last + upperHalf.head) / 2.0 else upperHalf.head
    BigDecimal(median).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

}
