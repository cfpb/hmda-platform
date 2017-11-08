package hmda.persistence.messages.events.processing

import hmda.model.fi.SubmissionId

object PubSubEvents {
  case class SubmissionSignedPubSub(submissionId: SubmissionId)
}
