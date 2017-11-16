package hmda.publication.submission.lar

import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Subscribe, SubscribeAck}
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.PubSubTopics

object SubmissionSignedModifiedLarSubscriber {
  val name = "SubmissionSignedModifiedLarSubscriber"
}

class SubmissionSignedModifiedLarSubscriber extends HmdaActor {

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)


  def receive: Receive = {
    case s:String =>
      log.info("Got {}", s)

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info("Subscribed to {}", PubSubTopics.submissionSigned)

    case SubmissionSignedPubSub(submissionId) =>
      log.info("Received submission signed event with submission id: {}", submissionId.toString)
  }
}
