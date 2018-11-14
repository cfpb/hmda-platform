package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowValidatedError,
  SubmissionProcessingEvent
}
import hmda.model.validation.{Macro, Quality, Syntactical, Validity}

case class HmdaValidationErrorState(totalErrors: Int = 0,
                                    syntacticalErrors: Int = 0,
                                    validityErrors: Int = 0,
                                    qualityErrors: Int = 0,
                                    macroErrors: Int = 0) {
  def update(event: SubmissionProcessingEvent): HmdaValidationErrorState =
    event match {
      //TODO: update state
//      case HmdaRowValidatedError(_, validationErrors) =>
//        validationErrors.head.validationErrorType match {
//          case Syntactical =>
//            this.copy(totalErrors = this.totalErrors + 1,
//                      syntacticalErrors = this.syntacticalErrors + 1)
//          case Validity =>
//            this.copy(totalErrors = this.totalErrors + 1,
//                      validityErrors = this.validityErrors + 1)
//          case Quality =>
//            this.copy(totalErrors = this.totalErrors + 1,
//                      qualityErrors = this.qualityErrors + 1)
//          case Macro =>
//            this.copy(totalErrors = this.totalErrors + 1,
//                      macroErrors = this.macroErrors + 1)
//        }

      case _ => this
    }
}
