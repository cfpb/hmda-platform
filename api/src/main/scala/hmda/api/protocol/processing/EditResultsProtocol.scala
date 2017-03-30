package hmda.api.protocol.processing

import hmda.api.model._
import hmda.api.protocol.validation.ValidationResultProtocol

trait EditResultsProtocol extends ValidationResultProtocol with SubmissionProtocol {
  implicit val larIdFormat = jsonFormat1(RowId.apply)
  implicit val larEditResultFormat = jsonFormat2(EditResultRow.apply)
  implicit val editResultFormat = jsonFormat3(EditResult.apply)
  implicit val editInfoFormat = jsonFormat2(EditInfo.apply)
  implicit val editCollectionFormat = jsonFormat1(EditCollection.apply)
  implicit val verifiableEditCollectionFormat = jsonFormat2(VerifiableEditCollection.apply)
  implicit val summaryEditResultsFormat = jsonFormat5(SummaryEditResults.apply)
  implicit val singleEditTypeFormat = jsonFormat2(SingleTypeEditResults.apply)
  implicit val editsVerificationFormat = jsonFormat1(EditsVerification.apply)
  implicit val editsVerifiedResponseFormat = jsonFormat2(EditsVerifiedResponse.apply)
}
