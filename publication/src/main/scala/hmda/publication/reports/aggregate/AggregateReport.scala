package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.ReportTypeEnum
import hmda.model.publication.reports.ReportTypeEnum.Aggregate
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

case class AggregateReportPayload(
  reportID: String,
  msa: String,
  report: String
)

trait AggregateReport {

  val reportType: ReportTypeEnum = Aggregate

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[AggregateReportPayload]

}
