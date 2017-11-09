package hmda.persistence.serialization.pubsub

import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.serialization.PubSubEvents.SubmissionSignedPubSubMessage
import hmda.persistence.model.serialization.SubmissionEvents.SubmissionIdMessage
import hmda.persistence.serialization.submission.SubmissionProtobufConverter._

object PubSubEventsProtobufConverter {
  def submissionSignedPubSubToProtobuf(obj: SubmissionSignedPubSub): SubmissionSignedPubSubMessage = {
    SubmissionSignedPubSubMessage(
      submissionId = Some(SubmissionIdMessage(obj.submissionId.institutionId, obj.submissionId.period, obj.submissionId.sequenceNumber))
    )
  }

  def submissionSignedPubSubFromProtobuf(msg: SubmissionSignedPubSubMessage): SubmissionSignedPubSub = {
    SubmissionSignedPubSub(
      submissionId = submissionIdFromProtobuf(msg.submissionId.getOrElse(SubmissionIdMessage()))
    )
  }
}
