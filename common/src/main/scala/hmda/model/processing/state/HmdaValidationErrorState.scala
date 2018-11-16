package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowValidatedError,
  MacroVerified,
  QualityVerified
}
import hmda.model.validation._

case class EditSummary(editName: String,
                       editType: ValidationErrorType,
                       entityType: ValidationErrorEntity)

case class HmdaValidationErrorState(statusCode: Int = 1,
                                    syntactical: Set[EditSummary] = Set.empty,
                                    validity: Set[EditSummary] = Set.empty,
                                    quality: Set[EditSummary] = Set.empty,
                                    `macro`: Set[EditSummary] = Set.empty,
                                    qualityVerified: Boolean = false,
                                    //TODO: change this default to false when macro is implemented
                                    macroVerified: Boolean = true) {
  def updateErrors(
      hmdaRowError: HmdaRowValidatedError): HmdaValidationErrorState = {

    val editSummaries = hmdaRowError.validationErrors
      .map { e =>
        EditSummary(
          e.editName,
          e.validationErrorType,
          e.validationErrorEntity
        )
      }
      .groupBy(_.editType)

    HmdaValidationErrorState(
      this.statusCode,
      this.syntactical ++ editSummaries.getOrElse(Syntactical, Nil).toSet,
      this.validity ++ editSummaries.getOrElse(Validity, Nil).toSet,
      this.quality ++ editSummaries.getOrElse(Quality, Nil).toSet,
      this.`macro` ++ editSummaries.getOrElse(Macro, Nil).toSet
    )
  }

  def verifyQuality(evt: QualityVerified): HmdaValidationErrorState =
    this.copy(qualityVerified = evt.verified)

  def verifyMacro(evt: MacroVerified): HmdaValidationErrorState =
    this.copy(macroVerified = evt.verified)

  def updateStatusCode(code: Int): HmdaValidationErrorState =
    this.copy(statusCode = code)

}
