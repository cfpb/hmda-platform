package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaMacroValidatedError,
  HmdaRowValidatedError,
  MacroVerified,
  QualityVerified
}
import hmda.model.filing.submission.{MacroErrors, QualityErrors, Verified}
import hmda.model.validation._

case class EditSummary(editName: String,
                       editType: ValidationErrorType,
                       entityType: ValidationErrorEntity)

case class HmdaValidationErrorState(statusCode: Int = 1,
                                    syntactical: Set[EditSummary] = Set.empty,
                                    validity: Set[EditSummary] = Set.empty,
                                    quality: Set[EditSummary] = Set.empty,
                                    `macro`: Set[EditSummary] = Set.empty,
                                    qualityVerified: Boolean = true,
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

    val qualityErrors = this.quality ++ editSummaries
      .getOrElse(Quality, Nil)
      .toSet
    val macroErrors = this.`macro` ++ editSummaries.getOrElse(Macro, Nil).toSet

    val updatedState = HmdaValidationErrorState(
      this.statusCode,
      this.syntactical ++ editSummaries.getOrElse(Syntactical, Nil).toSet,
      this.validity ++ editSummaries.getOrElse(Validity, Nil).toSet,
      qualityErrors,
      macroErrors,
      qualityVerified = qualityErrors.isEmpty,
      macroVerified = macroErrors.isEmpty
    )
    println(
      s"\nState is at ${updatedState.statusCode} with ${qualityErrors.toList.length} quality errors, ${macroErrors.toList.length} macro errors, and quality/macro verified is ${updatedState.qualityVerified}/${updatedState.macroVerified}\n")
    updatedState
  }

  def updateMacroErrors(
      error: HmdaMacroValidatedError): HmdaValidationErrorState = {
    val updatedState = this.copy(
      `macro` = this.`macro` ++ Set(
        EditSummary(error.error.editName, Macro, LarValidationError)),
      macroVerified = false)
    println(
      s"\nState is at ${updatedState.statusCode} with ${updatedState.quality.toList.length} quality errors, ${updatedState.`macro`.toList.length} macro errors, and quality/macro verified is ${updatedState.qualityVerified}/${updatedState.macroVerified}\n")
    updatedState
  }

  def verifyQuality(evt: QualityVerified): HmdaValidationErrorState = {
    val status = if (evt.verified) {
      if (macroVerified) Verified.code
      else if (`macro`.isEmpty) hmda.model.filing.submission.Macro.code
      else MacroErrors.code
    } else {
      if (quality.isEmpty) hmda.model.filing.submission.Quality.code
      else QualityErrors.code
    }
    this.copy(qualityVerified = evt.verified, statusCode = status)
  }

  def verifyMacro(evt: MacroVerified): HmdaValidationErrorState = {
    val status =
      if (evt.verified) Verified.code
      else if (`macro`.isEmpty) hmda.model.filing.submission.Macro.code
      else MacroErrors.code
    this.copy(macroVerified = evt.verified, statusCode = status)
  }

  def updateStatusCode(code: Int): HmdaValidationErrorState =
    this.copy(statusCode = code)

  def noEditsFound(): Boolean =
    syntactical.isEmpty && validity.isEmpty && quality.isEmpty && `macro`.isEmpty

}
