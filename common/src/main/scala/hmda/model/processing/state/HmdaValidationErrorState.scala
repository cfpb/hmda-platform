package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowValidatedError
import hmda.model.validation._

case class EditSummary(editName: String,
                       editType: ValidationErrorType,
                       entityType: ValidationErrorEntity)

case class HmdaValidationErrorState(syntactical: Set[EditSummary] = Set.empty,
                                    validity: Set[EditSummary] = Set.empty,
                                    quality: Set[EditSummary] = Set.empty,
                                    `macro`: Set[EditSummary] = Set.empty) {
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
      this.syntactical ++ editSummaries.getOrElse(Syntactical, Nil).toSet,
      this.validity ++ editSummaries.getOrElse(Validity, Nil).toSet,
      this.quality ++ editSummaries.getOrElse(Quality, Nil).toSet,
      this.`macro` ++ editSummaries.getOrElse(Macro, Nil).toSet
    )
  }

}
