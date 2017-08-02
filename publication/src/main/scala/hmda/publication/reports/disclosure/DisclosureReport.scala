package hmda.publication.reports.disclosure

import hmda.model.publication.reports.{ MSAReport, ReportTypeEnum }
import hmda.model.publication.reports.ReportTypeEnum.Disclosure

trait DisclosureReport {

  val respondentId: String
  val institutionName: String
  val description: String
  val table: String
  val year: Int
  val msa: MSAReport

  val reportType: ReportTypeEnum = Disclosure

}
