package hmda.publication.reports.disclosure

import java.util.Calendar

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.publication.reports._
import hmda.model.publication.reports._
import hmda.publication.reports.util.DateUtil._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class D51(
  respondentId: String,
  institutionName: String,
  year: Int,
  reportDate: String,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition],
  table: String = D51.metaData.reportTable,
  description: String = D51.metaData.description
) extends DisclosureReport

object D51 {
  val metaData = ReportsMetaDataLookup.values("D51")
  val dispositions = metaData.dispositions

  // Table filters:
  // Loan Type 2,3,4
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int,
    respondentId: String,
    institutionNameF: Future[String]
  ): Future[D51] = {

    val lars = larSource
      .filter(lar => lar.respondentId == respondentId)
      .filter(lar => lar.msa != "NA")
      .filter(lar => lar.msa.toInt == fipsCode)
      .filter { lar =>
        (lar.loanType == 2 || lar.loanType == 3 || lar.loanType == 4) &&
          (lar.propertyType == 1 || lar.propertyType == 2) &&
          (lar.purpose == 1)
      }
    val larsWithIncome = lars.filter(lar => lar.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)
    val applicantIncomesF = applicantIncomesWithBorrowerCharacteristics(larsWithIncome, incomeIntervals, dispositions)

    val yearF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      institutionName <- institutionNameF
      year <- yearF
      applicantIncomes <- applicantIncomesF
      total <- totalF
    } yield {

      D51(
        respondentId,
        institutionName,
        year,
        formatDate(Calendar.getInstance().toInstant),
        msa,
        applicantIncomes,
        total
      )
    }

  }
}
