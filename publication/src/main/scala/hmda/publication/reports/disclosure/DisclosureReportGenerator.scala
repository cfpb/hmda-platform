package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.RaceEnum._
import hmda.model.publication.reports.{ Disposition, MSAReport, RaceCharacteristic }
import hmda.publication.reports._

import scala.concurrent.Future
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.ReportUtil._

class DisclosureReportGenerator {

  def generateReports[as: AS, mat: MAT, ec: EC](larSource: Source[LoanApplicationRegister, NotUsed], fipsCode: Int, respId: String): Future[Unit] = {
    val d51 = genD51Report(larSource, fipsCode, respId)
    Future {
      ()
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

    val lars50Alaskan = filterRace(lars50, AmericanIndianOrAlaskaNative)
    val lars50Asian = filterRace(lars50, Asian)
    val lars50Black = filterRace(lars50, BlackOrAfricanAmerican)
    val lars50Hawaiian = filterRace(lars50, HawaiianOrPacific)
    val lars50White = filterRace(lars50, White)
    val larsTwoMinorities = filterRace(lars50, TwoOrMoreMinority)
    val lars50Joint = filterRace(lars50, Joint)
    val lars50NotProvided = filterRace(lars50, NotProvided)

    val disp50AlaskanF = calculateDispositions(lars50Alaskan)

    val dateF = calculateDate(larSource)
    val totalF = calculateDispositions(lars)

    for {
      disp50laskan <- disp50AlaskanF
      date <- dateF
      total <- totalF
    } yield {
      val alaskan50Characteristic = RaceCharacteristic(AmericanIndianOrAlaskaNative, disp50laskan)
      D51(
        respId,
        "",
        date,
        formatDate(Calendar.getInstance().toInstant),
        msa,
        Nil,
        total
      )
    }

  }

}
