package hmda.api.protocol.processing

import hmda.api.model._
import hmda.api.protocol.validation.ValidationResultProtocol
import spray.json._

trait EditResultsProtocol extends ValidationResultProtocol with SubmissionProtocol with PaginationProtocol {
  implicit val larIdFormat = jsonFormat1(RowId.apply)
  implicit val larEditResultFormat = jsonFormat2(EditResultRow.apply)
  implicit val editInfoFormat = jsonFormat2(EditInfo.apply)
  implicit val editCollectionFormat = jsonFormat1(EditCollection.apply)
  implicit val verifiableEditCollectionFormat = jsonFormat2(VerifiableEditCollection.apply)
  implicit val summaryEditResultsFormat = jsonFormat5(SummaryEditResults.apply)
  implicit val singleEditTypeFormat = jsonFormat2(SingleTypeEditResults.apply)
  implicit val editsVerificationFormat = jsonFormat1(EditsVerification.apply)
  implicit val editsVerifiedResponseFormat = jsonFormat2(EditsVerifiedResponse.apply)

  implicit object EditResultJsonFormat extends RootJsonFormat[EditResult] {
    override def write(result: EditResult): JsValue = {
      JsObject(
        "edit" -> result.edit.toJson,
        "rows" -> result.rows.toJson,
        "count" -> result.count.toJson,
        "total" -> result.total.toJson,
        "_links" -> result.links.toJson
      )
    }

    override def read(json: JsValue): EditResult = {
      json.asJsObject.getFields("edit", "rows", "count", "total", "_links") match {
        case Seq(JsString(editName), JsArray(rows), JsNumber(_), JsNumber(tot), JsObject(links)) =>
          val resultRows: Seq[EditResultRow] = rows.map(_.convertTo[EditResultRow])
          val path: String = PaginatedResponse.staticPath(links("href").convertTo[String])
          val currentPage: Int = PaginatedResponse.currentPage(links("self").convertTo[String])
          val total: Int = tot.intValue
          EditResult(editName, resultRows, path, currentPage, total)

        case _ => throw DeserializationException("Edit Result expected")
      }
    }
  }

}
