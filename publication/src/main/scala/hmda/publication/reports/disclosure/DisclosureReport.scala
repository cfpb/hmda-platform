package hmda.publication.reports.disclosure

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.model.publication.reports.ReportTypeEnum
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.publication.reports._

import scala.concurrent.Future

trait DisclosureReport {

  val reportType: ReportTypeEnum = Disclosure

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int,
    institution: Institution
  ): Future[DisclosureReportPayload]

}
