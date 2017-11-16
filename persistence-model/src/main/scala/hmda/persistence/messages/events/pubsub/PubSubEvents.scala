package hmda.persistence.messages.events.pubsub

import hmda.model.fi.SubmissionId

object PubSubEvents {
  case class SubmissionSignedPubSub(submissionId: SubmissionId)
  case class SubmissionLarProjectedPubSub(submissionId: SubmissionId)
}
