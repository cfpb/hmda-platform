package hmda.serialization.filing

import hmda.persistence.filing.FilingState
import hmda.persistence.serialization.filing.FilingMessage
import hmda.persistence.serialization.filing.filingstate.FilingStateMessage
import hmda.serialization.filing.FilingProtobufConverter._
import hmda.serialization.submission.SubmissionProtobufConverter._

object FilingStateProtobufConverter {

  def filingStateToProtobuf(filingState: FilingState): FilingStateMessage =
    FilingStateMessage(
      if (filingState.filing.isEmpty) None
      else Some(filingToProtobuf(filingState.filing)),
      filingState.submissions.map(s => submissionToProtobuf(s))
    )

  def filingStateFromProtobuf(filingStateMessage: FilingStateMessage): FilingState =
    FilingState(
      filing = filingFromProtobuf(filingStateMessage.filing.getOrElse(FilingMessage())),
      submissions = filingStateMessage.submissions
        .map(s => submissionFromProtobuf(s))
        .toList
    )
}
