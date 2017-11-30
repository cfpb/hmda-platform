package hmda.publication.reports.protocol.aggregate

import hmda.model.publication.reports.{ ApplicantIncome, ValueDisposition, MSAReport }
import hmda.model.publication.reports.ReportTypeEnum._
import hmda.publication.reports.aggregate.A5X
import hmda.publication.reports.protocol.{ ApplicantIncomeProtocol, MSAReportProtocol, ReportTypeEnumProtocol }
import spray.json._

object A5XProtocol
    extends DefaultJsonProtocol
    with ReportTypeEnumProtocol
    with MSAReportProtocol
    with ApplicantIncomeProtocol {

  implicit object A5XFormat extends RootJsonFormat[A5X] {
    override def write(obj: A5X): JsValue = {
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

    override def read(json: JsValue): A5X = json.asJsObject.getFields(
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
          A5X(
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
