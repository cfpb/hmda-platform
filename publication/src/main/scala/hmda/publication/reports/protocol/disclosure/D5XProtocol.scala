package hmda.publication.reports.protocol.disclosure

import hmda.model.publication.reports.{ ApplicantIncome, ValueDisposition, MSAReport }
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.publication.reports.disclosure.D5X
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._
import spray.json.DefaultJsonProtocol

object D5XProtocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object D5XFormat extends RootJsonFormat[D5X] {

    override def write(obj: D5X): JsValue = {
      JsObject(
        "respondentId" -> JsString(obj.respondentId),
        "institutionName" -> JsString(obj.institutionName),
        "table" -> JsString(obj.table),
        "type" -> JsString(Disclosure.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "reportDate" -> JsString(obj.reportDate),
        "msa" -> obj.msa.toJson,
        "applicantIncomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): D5X = json.asJsObject.getFields(
      "respondentId",
      "institutionName",
      "year",
      "reportDate",
      "msa",
      "applicantIncomes",
      "total",
      "table",
      "desc"
    ) match {
        case Seq(respondentId, institutionName, year, reportDate, msa, applicantIncomes, total, table, desc) =>
          D5X(
            respondentId.convertTo[String],
            institutionName.convertTo[String],
            year.convertTo[Int],
            msa.convertTo[MSAReport],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[ValueDisposition]],
            table.convertTo[String],
            desc.convertTo[String],
            reportDate.convertTo[String]
          )
      }
  }
}
