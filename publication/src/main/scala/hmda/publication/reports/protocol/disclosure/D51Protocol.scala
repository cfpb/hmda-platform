package hmda.publication.reports.protocol.disclosure

import spray.json.DefaultJsonProtocol
import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.model.publication.reports.ReportTypeEnum.Disclosure
import hmda.publication.reports.disclosure.D51
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._

object D51Protocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object D51Format extends RootJsonFormat[D51] {

    override def write(obj: D51): JsValue = {
      JsObject(
        "respondent_id" -> JsString(obj.respondentId),
        "institution_name" -> JsString(obj.institutionName),
        "table" -> JsString("5-1"),
        "type" -> JsString(Disclosure.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "report_date" -> JsString(obj.reportDate),
        "msa" -> obj.msa.toJson,
        "applicant_incomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): D51 = json.asJsObject.getFields(
      "respondent_id",
      "institution_name",
      "table",
      "type",
      "desc",
      "year",
      "report_date",
      "msa",
      "applicant_incomes",
      "total"
    ) match {
        case Seq(respondentId, institutionName, table, reportType, description, year, reportDate, msa, applicantIncomes, total) =>
          D51(
            respondentId.convertTo[String],
            institutionName.convertTo[String],
            year.convertTo[Int],
            reportDate.convertTo[String],
            msa.convertTo[MSAReport],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[Disposition]]
          )
      }
  }
}
