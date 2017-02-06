package hmda.api.protocol.processing

import hmda.api.model._
import hmda.api.protocol.validation.ValidationResultProtocol
import spray.json.DefaultJsonProtocol

trait EditResultsProtocol extends ValidationResultProtocol {
  implicit val larIdFormat = jsonFormat1(RowId.apply)
  implicit val larEditResultFormat = jsonFormat2(EditResultRow.apply)
  implicit val editResultFormat = jsonFormat3(EditResult.apply)
  implicit val editResultsFormat = jsonFormat1(EditResults.apply)
  implicit val qualityEditResultsFormat = jsonFormat2(QualityEditResults.apply)
  implicit val rowEditDetailFormat = jsonFormat3(RowEditDetail.apply)
  implicit val rowResultFormat = jsonFormat2(RowResult.apply)
  implicit val macroResultFormat = jsonFormat2(MacroResult.apply)
  implicit val macroResultsFormat = jsonFormat1(MacroResults.apply)
  implicit val rowResultsFormat = jsonFormat2(RowResults.apply)
  implicit val summaryEditResultsFormat = jsonFormat4(SummaryEditResults.apply)
}
