package hmda.publication.reports.national

import hmda.model.publication.reports.ReportTypeEnum.NationalAggregate
import hmda.model.publication.reports.ReportTypeEnum

trait NationalAggregateReport {

  val description: String
  val table: String
  val year: Int

  val reportType: ReportTypeEnum = NationalAggregate

}
