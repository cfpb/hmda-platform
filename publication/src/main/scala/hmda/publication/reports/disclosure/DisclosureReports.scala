package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ApplicantIncomeEnum._
import hmda.model.publication.reports._
import hmda.publication.reports._

import scala.concurrent.Future
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.ReportUtil._
import spray.json._
import hmda.publication.reports.protocol.disclosure.D51Protocol._

class DisclosureReports() {

  def generateReports[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegister, NotUsed], fipsCode: Int, respId: String): Future[Unit] = {
    val d51F = genD51Report(larSource, fipsCode, respId)
    d51F.map { d51 =>
      println(d51.toJson.prettyPrint)
    }

  }

  // Table filters:
  // Loan Type 2,3,4
  // Property Type 1,2
  // Purpose of Loan 1
  private def genD51Report[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegister, NotUsed], fipsCode: Int, respId: String): Future[D51] = {

    val lars = larSource
      .filter(lar => lar.respondentId == respId)
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter { lar =>
        (lar.loan.loanType == 2 || lar.loan.loanType == 3 || lar.loan.loanType == 4) &&
          (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
          (lar.loan.purpose == 1)
      }

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)

    val lars50 = lars
      .filter(lar => lar.applicant.income != "NA")
      .filter(lar => lar.applicant.income.toInt < incomeIntervals(0))

    val lars50To79 = lars
      .filter(lar => lar.applicant.income != "NA")
      .filter(lar => lar.applicant.income.toInt > incomeIntervals(0) && lar.applicant.income.toInt < incomeIntervals(1))

    val lars80To99 = lars
      .filter(lar => lar.applicant.income != "NA")
      .filter(lar => lar.applicant.income.toInt > incomeIntervals(1) && lar.applicant.income.toInt < incomeIntervals(2))

    val lars100to120 = lars
      .filter(lar => lar.applicant.income != "NA")
      .filter(lar => lar.applicant.income.toInt > incomeIntervals(2) && lar.applicant.income.toInt < incomeIntervals(3))

    val lars120 = lars
      .filter(lar => lar.applicant.income != "NA")
      .filter(lar => lar.applicant.income.toInt > incomeIntervals(4))

    val dateF = calculateDate(larSource)
    val totalF = calculateDispositions(lars)

    for {
      races50 <- raceBorrowerCharacteristic(lars50, LessThan50PercentOfMSAMedian)
      races50to79 <- raceBorrowerCharacteristic(lars50To79, Between50And79PercentOfMSAMedian)
      races80to99 <- raceBorrowerCharacteristic(lars80To99, Between80And99PercentOfMSAMedian)
      races100to120 <- raceBorrowerCharacteristic(lars100to120, Between100And119PercentOfMSAMedian)
      races120 <- raceBorrowerCharacteristic(lars120, GreaterThan120PercentOfMSAMedian)
      date <- dateF
      total <- totalF
    } yield {
      val income50 = ApplicantIncome(
        LessThan50PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races50)
        )
      )
      val income50To79 = ApplicantIncome(
        Between50And79PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races50to79)
        )
      )
      val income80To99 = ApplicantIncome(
        Between80And99PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races80to99)
        )
      )
      val income100To120 = ApplicantIncome(
        Between100And119PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races100to120)
        )
      )
      val income120 = ApplicantIncome(
        GreaterThan120PercentOfMSAMedian,
        List(
          RaceBorrowerCharacteristic(races120)
        )
      )

      D51(
        respId,
        "",
        date,
        formatDate(Calendar.getInstance().toInstant),
        msa,
        List(
          income50,
          income50To79,
          income80To99,
          income100To120,
          income120
        ),
        total
      )
    }

  }

}
