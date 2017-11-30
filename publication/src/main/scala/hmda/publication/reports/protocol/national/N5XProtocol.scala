package hmda.publication.reports.protocol.national

import hmda.model.publication.reports.ReportTypeEnum._
import hmda.model.publication.reports.{ ApplicantIncome, ValueDisposition }
import hmda.publication.reports.national.N5X
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._

object N5XProtocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object N5XFormat extends RootJsonFormat[N5X] {
    override def write(obj: N5X): JsValue = {
      JsObject(
        "table" -> JsString(obj.table),
        "type" -> JsString(NationalAggregate.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "reportDate" -> JsString(obj.reportDate),
        "applicantIncomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): N5X = json.asJsObject.getFields(
      "table",
      "type",
      "desc",
      "year",
      "reportDate",
      "applicantIncomes",
      "total"
    ) match {
        case Seq(table, reportType, description, year, reportDate, applicantIncomes, total) =>
          N5X(
            year.convertTo[Int],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[ValueDisposition]],
            table.convertTo[String],
            description.convertTo[String],
            reportDate.convertTo[String]
          )
      }
  }

}
