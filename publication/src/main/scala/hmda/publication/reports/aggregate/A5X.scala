package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.{ ApplicantIncome, ValueDisposition, MSAReport }
import hmda.publication.reports._
import hmda.publication.reports.protocol.aggregate.A5XReportProtocol._
import hmda.publication.reports.util.ReportUtil._
import hmda.publication.reports.util.ReportsMetaDataLookup

import scala.concurrent.Future
import spray.json._

object A52 extends A5X {
  val reportId = "A52"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.loanType == 1) &&
      (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 1)
  }
}

object A53 extends A5X {
  val reportId = "A53"
  def filters(lar: LoanApplicationRegister): Boolean = {
    (lar.loan.propertyType == 1 || lar.loan.propertyType == 2) &&
      (lar.loan.purpose == 3)
  }
}

case class A5XReport(
  year: Int,
  msa: MSAReport,
  applicantIncomes: List[ApplicantIncome],
  total: List[ValueDisposition],
  table: String,
  description: String,
  reportDate: String = formattedCurrentDate
)

trait A5X extends AggregateReport {
  val reportId: String
  def filters(lar: LoanApplicationRegister): Boolean

  override def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload] = {

    generateA5X(larSource, fipsCode).map { a5x =>
      val report = a5x.toJson.toString
      AggregateReportPayload(reportId, fipsCode.toString, report)
    }
  }

  def generateA5X[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[A5XReport] = {

    val metaData = ReportsMetaDataLookup.values(reportId)
    val dispositions = metaData.dispositions

    val lars = larSource
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
      applicantIncomes <- applicantIncomesF
      year <- yearF
      total <- totalF
    } yield {

      A5XReport(
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
