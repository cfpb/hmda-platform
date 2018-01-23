package hmda.publication.reports.disclosure

import hmda.model.publication.reports.{ ApplicantIncome, MSAReport, ValueDisposition }
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.publication.reports.util.ReportUtil._
import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.publication.reports._

import scala.concurrent.Future

case class D5X(
  respondentId: String,
  institutionName: String,
  year: Int,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[ValueDisposition],
  table: String,
  description: String,
  reportDate: String = formattedCurrentDate
) extends DisclosureReport

object D5X {
  def generateD5X[ec: EC, mat: MAT, as: AS](
    reportId: String,
    filters: LoanApplicationRegister => Boolean,
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[D5X] = {

    val metaData = ReportsMetaDataLookup.values(reportId)
    val dispositions = metaData.dispositions

    val lars = larSource
      .filter(lar => lar.respondentId == institution.respondentId)
      .filter(lar => lar.geography.msa != "NA")
      .filter(lar => lar.geography.msa.toInt == fipsCode)
      .filter(filters)

    val larsWithIncome = lars.filter(lar => lar.applicant.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)
    val applicantIncomesF = applicantIncomesWithBorrowerCharacteristics(larsWithIncome, incomeIntervals, dispositions)

    val yearF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      year <- yearF
      applicantIncomes <- applicantIncomesF
      total <- totalF
    } yield {

      D5X(
        institution.respondentId,
        institution.respondent.name,
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
