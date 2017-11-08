package hmda.persistence.serialization.pubsub

import hmda.persistence.messages.events.processing.PubSubEvents.SubmissionSigned
import hmda.persistence.model.serialization.PubSubEvents.SubmissionSignedPubSubMessage
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

object PubSubEventsProtobufConverter {
  def submissionSignedPubSubToProtobuf(obj: SubmissionSigned): SubmissionSignedPubSubMessage = {
    SubmissionSignedPubSubMessage(
      submissionId = Some(SubmissionIdMessage(obj.submissionId.institutionId, obj.submissionId.period, obj.submissionId.sequenceNumber))
    )
  }

  def submissionSignedPubSubFromProtobuf(msg: SubmissionSignedPubSubMessage): SubmissionSigned = {
    SubmissionSigned(
      submissionId = submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }
}
