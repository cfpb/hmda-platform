package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.publication.reports._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup
import hmda.query.model.filing.LoanApplicationRegisterQuery

import scala.concurrent.Future

case class A52(
  year: Int,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[Disposition],
  reportDate: String = formattedCurrentDate,
  table: String = A52.metaData.reportTable,
  description: String = A52.metaData.description
) extends AggregateReport

object A52 {
  val metaData = ReportsMetaDataLookup.values("A52")
  val dispositions = metaData.dispositions

  // Table filters:
  // Loan Type 1
  // Property Type 1,2
  // Purpose of Loan 1
  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegisterQuery, NotUsed],
    fipsCode: Int
  ): Future[A52] = {
    val lars = larSource
      .filter(lar => lar.msa != "NA")
      .filter(lar => lar.msa.toInt == fipsCode)
      .filter { lar =>
        (lar.loanType == 1) &&
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
      applicantIncomes <- applicantIncomesF
      year <- yearF
      total <- totalF
    } yield {
      A52(
        year,
        msa,
        applicantIncomes,
        total
      )
    }
  }
}
