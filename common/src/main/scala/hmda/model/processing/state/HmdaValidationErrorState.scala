package hmda.model.processing.state

import hmda.messages.submission.SubmissionProcessingEvents.SubmissionProcessingEvent

case class HmdaValidationErrorState(totalErrors: Int = 0,
                                    syntacticalErrors: Int = 0,
                                    validityErrors: Int = 0,
                                    qualityErrors: Int = 0,
                                    macroErrors: Int = 0) {
  def update(event: SubmissionProcessingEvent): HmdaValidationErrorState = this
}
