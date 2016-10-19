package hmda.api.protocol.processing

import hmda.api.model._
import spray.json.DefaultJsonProtocol

trait EditResultsProtocol extends DefaultJsonProtocol {
  implicit val larIdFormat = jsonFormat1(LarId.apply)
  implicit val larEditResultFormat = jsonFormat1(LarEditResult.apply)
  implicit val editResultFormat = jsonFormat3(EditResult.apply)
  implicit val editResultsFormat = jsonFormat1(EditResults.apply)
  implicit val summaryEditResultsFormat = jsonFormat4(SummaryEditResults.apply)

}
