package hmda.publication.reports.disclosure

import hmda.publication.reports._
import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport, ReportTypeEnum }
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.util.ReportUtil._
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

trait DisclosureReport {

  val respondentId: String
  val institutionName: String
  val description: String
  val table: String
  val year: Int
  val msa: MSAReport
  val reportDate: String

  val reportType: ReportTypeEnum = Disclosure

}

case class D5X(
  respondentId: String,
  institutionName: String,
  year: Int,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition],
  table: String,
  description: String,
  reportDate: String = formattedCurrentDate
) extends DisclosureReport

object D5X {
  def generate[ec: EC, mat: MAT, as: AS](
    reportId: String,
    filters: LoanApplicationRegisterQuery => Boolean,
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int,
    respondentId: String,
    institutionNameF: Future[String]
  ): Future[D5X] = {

    val metaData = ReportsMetaDataLookup.values(reportId)
    val dispositions = metaData.dispositions

    val lars = larSource
      .filter(lar => lar.respondentId == respondentId)
      .filter(lar => lar.msa != "NA")
      .filter(lar => lar.msa.toInt == fipsCode)
      .filter(filters)

    val larsWithIncome = lars.filter(lar => lar.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = incomeIntervalsForMsa(fipsCode)
    val applicantIncomesF = applicantIncomesByInterval(larsWithIncome, incomeIntervals, dispositions)

    val yearF = reportYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      institutionName <- institutionNameF
      year <- yearF
      applicantIncomes <- applicantIncomesF
      total <- totalF
    } yield {

      D5X(
        respondentId,
        institutionName,
        year,
        msa,
        applicantIncomes,
        total,
        metaData.reportTable,
        metaData.description
      )
    }

  }
}