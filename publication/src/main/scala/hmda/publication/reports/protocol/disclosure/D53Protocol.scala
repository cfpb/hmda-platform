package hmda.publication.reports.protocol.disclosure

import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.publication.reports.disclosure.D53
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }

import spray.json._
import spray.json.DefaultJsonProtocol

object D53Protocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object D53Format extends RootJsonFormat[D53] {

    override def write(obj: D53): JsValue = {
      JsObject(
        "respondentId" -> JsString(obj.respondentId),
        "institutionName" -> JsString(obj.institutionName),
        "table" -> JsString("5-3"),
        "type" -> JsString(Disclosure.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "reportDate" -> JsString(obj.reportDate),
        "msa" -> obj.msa.toJson,
        "applicantIncomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): D53 = json.asJsObject.getFields(
      "respondentId",
      "institutionName",
      "year",
      "reportDate",
      "msa",
      "applicantIncomes",
      "total"
    ) match {
        case Seq(respondentId, institutionName, year, reportDate, msa, applicantIncomes, total) =>
          D53(
            respondentId.convertTo[String],
            institutionName.convertTo[String],
            year.convertTo[Int],
            msa.convertTo[MSAReport],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[Disposition]],
            reportDate.convertTo[String]
          )
      }
  }
}
