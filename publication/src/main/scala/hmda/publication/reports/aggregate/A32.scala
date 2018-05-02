package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ReportTypeEnum.Aggregate
import hmda.publication.reports._
import hmda.publication.reports.util.DispositionType._
import hmda.publication.reports.util.PricingDataUtil._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.{ DispositionType, ReportsMetaDataLookup }
import hmda.util.SourceUtils

import scala.concurrent.Future

object A32 extends A32X {
  override val reportId = "A32"
}

object N32 extends A32X {
  override val reportId = "N32"
}

trait A32X extends AggregateReport with SourceUtils {
  val reportId = "D32"
  def filters(lar: LoanApplicationRegister): Boolean = {
    lar.actionTakenType == 1 &&
      (1 to 9).contains(lar.purchaserType) &&
      (1 to 2).contains(lar.lienStatus)
  }

  def geoFilter(fips: Int)(lar: LoanApplicationRegister): Boolean =
    lar.geography.msa != "NA" &&
      lar.geography.msa.toInt == fips

  val dispositions = List(FannieMae, GinnieMae, FreddieMac,
    FarmerMac, PrivateSecuritization, CommercialBank,
    FinanceCompany, Affiliate, OtherPurchaser)

  override def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {

    val metaData = ReportsMetaDataLookup.values(reportId)

    val lars =
      if (metaData.reportType == Aggregate) larSource.filter(filters).filter(geoFilter(fipsCode))
      else larSource.filter(filters)

    val msa: String = if (metaData.reportType == Aggregate) s""""msa": ${msaReport(fipsCode.toString).toJsonFormat},""" else ""
    val reportDate = formattedCurrentDate
    val yearF = calculateYear(lars)

    for {
      year <- yearF

      p1 <- lienDispositions(lars.filter(_.rateSpread == "NA"))
      p2 <- lienDispositions(lars.filter(pricingDataReported))

      rs1_5 <- lienDispositions(lars.filter(rateSpreadBetween(1.5, 2)))
      rs2_0 <- lienDispositions(lars.filter(rateSpreadBetween(2, 2.5)))
      rs2_5 <- lienDispositions(lars.filter(rateSpreadBetween(2.5, 3)))
      rs3_0 <- lienDispositions(lars.filter(rateSpreadBetween(3, 3.5)))
      rs3_5 <- lienDispositions(lars.filter(rateSpreadBetween(3.5, 4.5)))
      rs4_5 <- lienDispositions(lars.filter(rateSpreadBetween(4.5, 5.5)))
      rs5_5 <- lienDispositions(lars.filter(rateSpreadBetween(5.5, 6.5)))
      rs6_5 <- lienDispositions(lars.filter(rateSpreadBetween(6.5, Int.MaxValue)))

      mean <- meanDispositions(lars.filter(pricingDataReported))
      median <- medianDispositions(lars.filter(pricingDataReported))

      hoepa <- lienDispositions(lars.filter(_.hoepaStatus == 1))
    } yield {
      val report =
        s"""
           |{
           |    "table": "${metaData.reportTable}",
           |    "type": "${metaData.reportType}",
           |    "description": "${metaData.description}",
           |    "year": "$year ",
           |    "reportDate": "$reportDate",
           |    $msa
           |    "pricingInformation": [
           |        {
           |            "pricing": "No reported pricing data",
           |            "purchasers": $p1
           |        },
           |        {
           |            "pricing": "reported pricing data",
           |            "purchasers": $p2
           |        }
           |    ],
           |    "points": [
           |        {
           |            "pricing": "1.50 - 1.99",
           |            "purchasers": $rs1_5
           |        },
           |        {
           |            "pricing": "2.00 - 2.49",
           |            "purchasers": $rs2_0
           |        },
           |        {
           |            "pricing": "2.50 - 2.99",
           |            "purchasers": $rs2_5
           |        },
           |        {
           |            "pricing": "3.00 - 3.49",
           |            "purchasers": $rs3_0
           |        },
           |        {
           |            "pricing": "3.50 - 4.49",
           |            "purchasers": $rs3_5
           |        },
           |        {
           |            "pricing": "4.50 - 5.49",
           |            "purchasers": $rs4_5
           |        },
           |        {
           |            "pricing": "5.50 - 6.49",
           |            "purchasers": $rs5_5
           |        },
           |        {
           |            "pricing": "6.5 or more",
           |            "purchasers": $rs6_5
           |        },
           |        {
           |            "pricing": "Mean",
           |            "purchasers": $mean
           |        },
           |        {
           |            "pricing": "Median",
           |            "purchasers": $median
           |        }
           |    ],
           |    "hoepa": {
           |        "pricing": "HOEPA loans",
           |        "purchasers": $hoepa
           |    }
           |}
           |
       """.stripMargin

      val fipsString = if (metaData.reportType == Aggregate) fipsCode.toString else "nationwide"

      AggregateReportPayload(metaData.reportTable, fipsString, report)
    }
  }

  private def lienDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val calculatedDispositions: Future[List[String]] = Future.sequence(
      dispositions.map(lienDispositionOutput(_, larSource))
    )

    calculatedDispositions.map(list => list.mkString("[", ",", "]"))
  }
  private def lienDispositionOutput[ec: EC, mat: MAT, as: AS](disposition: DispositionType, larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val larsFiltered = larSource.filter(disposition.filter)

    val firstLienLars = larsFiltered.filter(_.lienStatus == 1)
    val flCountF = count(firstLienLars)
    val flTotalF = sum(firstLienLars, loanAmount)

    val juniorLienLars = larsFiltered.filter(_.lienStatus == 2)
    val jlCountF = count(juniorLienLars)
    val jlTotalF = sum(juniorLienLars, loanAmount)

    for {
      flCount <- flCountF
      flTotal <- flTotalF
      jlCount <- jlCountF
      jlTotal <- jlTotalF
    } yield {
      s"""
         |{
         |    "disposition": "${disposition.value}",
         |    "firstLienCount": $flCount,
         |    "firstLienValue": $flTotal,
         |    "juniorLienCount": $jlCount,
         |    "juniorLienValue": $jlTotal
         |}
        """
    }
  }

  private def meanDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val calculatedDispositions: Future[List[String]] = Future.sequence(
      dispositions.map(meanDisposition(_, larSource))
    )

    calculatedDispositions.map(list => list.mkString("[", ",", "]"))
  }
  private def meanDisposition[ec: EC, mat: MAT, as: AS](
    disposition: DispositionType,
    larSource: Source[LoanApplicationRegister, NotUsed]
  ): Future[String] = {

    val larsFiltered = larSource.filter(disposition.filter).filter(pricingDataReported)

    val firstLienLars = larsFiltered.filter(_.lienStatus == 1)
    val flMeanF = calculateMean(firstLienLars, rateSpread)

    val juniorLienLars = larsFiltered.filter(_.lienStatus == 2)
    val jlMeanF = calculateMean(juniorLienLars, rateSpread)

    for {
      flMean <- flMeanF
      jlMean <- jlMeanF
    } yield {
      s"""
         |{
         |    "name": "${disposition.value}",
         |    "firstLienCount": $flMean,
         |    "firstLienValue": $flMean,
         |    "juniorLienCount": $jlMean,
         |    "juniorLienValue": $jlMean
         |}
       """.stripMargin
    }
  }

  private def medianDispositions[ec: EC, mat: MAT, as: AS](larSource: Source[LoanApplicationRegister, NotUsed]): Future[String] = {
    val calculatedDispositions: Future[List[String]] = Future.sequence(
      dispositions.map(medianDisposition(_, larSource))
    )

    calculatedDispositions.map(list => list.mkString("[", ",", "]"))
  }
  private def medianDisposition[ec: EC, mat: MAT, as: AS](
    disposition: DispositionType,
    larSource: Source[LoanApplicationRegister, NotUsed]
  ): Future[String] = {

    val larsFiltered = larSource.filter(disposition.filter).filter(pricingDataReported)

    val firstLienLars = larsFiltered.filter(_.lienStatus == 1)
    val flMedianF = calculateMedian(firstLienLars, rateSpread)

    val juniorLienLars = larsFiltered.filter(_.lienStatus == 2)
    val jlMedianF = calculateMedian(juniorLienLars, rateSpread)

    for {
      flMedian <- flMedianF
      jlMedian <- jlMedianF
    } yield {
      s"""
         |{
         |    "name": "${disposition.value}",
         |    "firstLienCount": $flMedian,
         |    "firstLienValue": $flMedian,
         |    "juniorLienCount": $jlMedian,
         |    "juniorLienValue": $jlMedian
         |}
       """.stripMargin
    }
  }
}
