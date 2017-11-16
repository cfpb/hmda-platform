package hmda.publication.submission.lar

import akka.actor.Props
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionLarProjectedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.PubSubTopics

object SubmissionSignedModifiedLarSubscriber {
  val name = "SubmissionSignedModifiedLarSubscriber"

  def props(): Props = Props(new SubmissionSignedModifiedLarSubscriber)

}

class SubmissionSignedModifiedLarSubscriber extends HmdaActor {

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionLarProjected, self)

  def receive: Receive = {
    case s: String =>
      log.info("Got {}", s)

    case SubscribeAck(Subscribe(PubSubTopics.submissionLarProjected, None, `self`)) =>
      log.info("Subscribed to {}", PubSubTopics.submissionLarProjected)

    case SubmissionLarProjectedPubSub(submissionId) =>
      log.info("Received submission lar projected event with submission id: {}", submissionId.toString)
      val institutionId = submissionId.institutionId


  }
}
