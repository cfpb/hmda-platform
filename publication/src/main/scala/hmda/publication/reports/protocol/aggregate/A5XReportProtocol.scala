package hmda.publication.reports.protocol.aggregate

import hmda.model.publication.reports.{ ApplicantIncome, MSAReport, ValueDisposition }
import hmda.model.publication.reports.ReportTypeEnum._
import hmda.publication.reports.aggregate.A5XReport
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._

object A5XReportProtocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object A5XReportFormat extends RootJsonFormat[A5XReport] {
    override def write(obj: A5XReport): JsValue = {
      JsObject(
        "table" -> JsString(obj.table),
        "type" -> JsString(Aggregate.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "reportDate" -> JsString(obj.reportDate),
        "msa" -> obj.msa.toJson,
        "applicantIncomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): A5XReport = json.asJsObject.getFields(
      "table",
      "type",
      "desc",
      "year",
      "reportDate",
      "msa",
      "applicantIncomes",
      "total"
    ) match {
        case Seq(table, reportType, description, year, reportDate, msa, applicantIncomes, total) =>
          A5XReport(
            year.convertTo[Int],
            msa.convertTo[MSAReport],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[ValueDisposition]],
            table.convertTo[String],
            description.convertTo[String],
            reportDate.convertTo[String]
          )
      }
  }

}
