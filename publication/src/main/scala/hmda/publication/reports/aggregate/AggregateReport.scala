package hmda.publication.reports.aggregate

import hmda.model.publication.reports.{ MSAReport, ReportTypeEnum }
import hmda.model.publication.reports.ReportTypeEnum.Aggregate

trait AggregateReport {

  val table: String
  val description: String
  val year: Int
  val msa: MSAReport
  val reportDate: String

  val reportType: ReportTypeEnum = Aggregate
}
