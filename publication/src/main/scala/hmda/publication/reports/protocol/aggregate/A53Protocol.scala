package hmda.publication.reports.protocol.aggregate

import hmda.model.publication.reports.{ ApplicantIncome, Disposition, MSAReport }
import hmda.model.publication.reports.ReportTypeEnum._
import hmda.publication.reports.aggregate.A53
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._

object A53Protocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object A53Format extends RootJsonFormat[A53] {
    override def write(obj: A53): JsValue = {
      JsObject(
        "table" -> JsString("5-3"),
        "type" -> JsString(Aggregate.toString),
        "desc" -> JsString(obj.description),
        "year" -> JsNumber(obj.year),
        "reportDate" -> JsString(obj.reportDate),
        "msa" -> obj.msa.toJson,
        "applicantIncomes" -> obj.applicantIncomes.toJson,
        "total" -> obj.total.toJson
      )
    }

    override def read(json: JsValue): A53 = json.asJsObject.getFields(
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
          A53(
            year.convertTo[Int],
            msa.convertTo[MSAReport],
            applicantIncomes.convertTo[List[ApplicantIncome]],
            total.convertTo[List[Disposition]],
            reportDate.convertTo[String]
          )
      }
  }

}
