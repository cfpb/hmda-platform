package hmda.publication.reports.aggregate

import akka.NotUsed
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.publication.reports.{ MSAReport, ReportTypeEnum }
import hmda.model.publication.reports.ReportTypeEnum.Aggregate
import hmda.publication.reports.{ AS, EC, MAT }

import scala.concurrent.Future

trait AggregateReport {

  val table: String
  val description: String
  val year: Int
  val msa: MSAReport
  val reportDate: String

  val reportType: ReportTypeEnum = Aggregate
}

abstract class A5XReportCreator {
  def filters(lar: LoanApplicationRegister): Boolean

  def generate[ec: EC, mat: MAT, as: AS](
    larSource: Source[LoanApplicationRegister, NotUsed],
    fipsCode: Int
  ): Future[A5X]
}
