package hmda.api.protocol.processing

import hmda.api.model._
import hmda.api.protocol.validation.ValidationResultProtocol

trait EditResultsProtocol extends ValidationResultProtocol with SubmissionProtocol {
  implicit val larIdFormat = jsonFormat1(RowId.apply)
  implicit val larEditResultFormat = jsonFormat2(EditResultRow.apply)
  implicit val editResultFormat = jsonFormat3(EditResult.apply)
  implicit val editResultsFormat = jsonFormat1(EditResults.apply)
  implicit val editResultsResponseFormat = jsonFormat2(EditResultsResponse.apply)
  implicit val qualityEditResultsFormat = jsonFormat2(QualityEditResults.apply)
  implicit val macroResultFormat = jsonFormat1(MacroResult.apply)
  implicit val macroResultsFormat = jsonFormat2(MacroResults.apply)
  implicit val macroResultsResponseFormat = jsonFormat2(MacroResultsResponse.apply)
  implicit val summaryEditResultsFormat = jsonFormat4(SummaryEditResults.apply)
  implicit val summaryEditResultsResponseFormat = jsonFormat5(SummaryEditResultsResponse.apply)
  implicit val editsVerificationFormat = jsonFormat1(EditsVerification.apply)
  implicit val editsVerifiedResponseFormat = jsonFormat2(EditsVerifiedResponse.apply)
}
