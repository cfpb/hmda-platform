package hmda.persistence.messages.events.pubsub

import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.Command

object PubSubEvents {
  case class SubmissionSignedPubSub(submissionId: SubmissionId)

  case class FindDisclosurePublisher() extends Command
  case class FindAggregatePublisher() extends Command
}
