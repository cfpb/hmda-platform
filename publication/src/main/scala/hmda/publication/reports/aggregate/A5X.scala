package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.publication.reports._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class A5X(
  year: Int,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition],
  table: String,
  description: String,
  reportDate: String = formattedCurrentDate
) extends AggregateReport

object A5X {
  def generate[ec: EC, mat: MAT, as: AS](
    reportId: String,
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int,
    filters: LoanApplicationRegisterQuery => Boolean
  ): Future[A5X] = {

    val metaData = ReportsMetaDataLookup.values(reportId)
    val dispositions = metaData.dispositions

    val lars = larSource
      .filter(lar => lar.msa != "NA")
      .filter(lar => lar.msa.toInt == fipsCode)
      .filter(filters)

    val larsWithIncome = lars.filter(lar => lar.income != "NA")

    val msa = msaReport(fipsCode.toString)

    val incomeIntervals = calculateMedianIncomeIntervals(fipsCode)
    val applicantIncomesF = applicantIncomesWithBorrowerCharacteristics(larsWithIncome, incomeIntervals, dispositions)

    val yearF = calculateYear(larSource)
    val totalF = calculateDispositions(lars, dispositions)

    for {
      applicantIncomes <- applicantIncomesF
      year <- yearF
      total <- totalF
    } yield {

      A5X(
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
