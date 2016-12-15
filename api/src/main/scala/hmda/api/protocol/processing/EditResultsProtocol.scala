package hmda.api.protocol.processing

import hmda.api.model._
import hmda.model.fi.RecordField
import spray.json.DefaultJsonProtocol

trait EditResultsProtocol extends DefaultJsonProtocol {
  implicit val larFieldFormat = jsonFormat2(RecordField.apply)
  implicit val larEditFieldFormat = jsonFormat2(LarEditField.apply)
  implicit val larEditResultFormat = jsonFormat2(LarEditResult.apply)
  implicit val editResultFormat = jsonFormat4(EditResult.apply)
  implicit val editResultsFormat = jsonFormat1(EditResults.apply)
  implicit val justificationFormat = jsonFormat2(Justification.apply)
  implicit val macroResultFormat = jsonFormat2(MacroResult.apply)
  implicit val macroResultsFormat = jsonFormat1(MacroResults.apply)
  implicit val summaryEditResultsFormat = jsonFormat4(SummaryEditResults.apply)
}
