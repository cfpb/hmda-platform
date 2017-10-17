package hmda.publication.reports.protocol.national

import hmda.model.publication.reports.ReportTypeEnum._
import hmda.model.publication.reports.{ ApplicantIncome, Disposition }
import hmda.publication.reports.national.N52
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._

object N52Protocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object N52Format extends RootJsonFormat[N52] {
    override def write(obj: N52): JsValue = {
      JsObject(
        "table" -> JsString("5-2"),
        "type" -> JsString(NationalAggregate.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "reportDate" -> JsString(obj.reportDate),
        "applicantIncomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): N52 = json.asJsObject.getFields(
      "table",
      "type",
      "desc",
      "year",
      "reportDate",
      "applicantIncomes",
      "total"
    ) match {
        case Seq(table, reportType, description, year, reportDate, applicantIncomes, total) =>
          N52(
            year.convertTo[Int],
            reportDate.convertTo[String],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[Disposition]]
          )
      }
  }

}
