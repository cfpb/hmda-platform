package hmda.query.projections.filing

import akka.actor.Props
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import hmda.persistence.messages.events.processing.PubSubEvents.SubmissionSigned
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.PubSubTopics

object SubmissionSignedEventQuerySubscriber {
  val name = "SubmissionSignedEventQuerySubscriber"
  def props(): Props = Props(new SubmissionSignedEventQuerySubscriber())
}

class SubmissionSignedEventQuerySubscriber() extends HmdaActor {

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  def receive: Receive = {

    case s: String =>
      log.info("Got {}", s)

    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info(s"Subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSigned(submissionId) =>
      log.info(s"Received submission signed event with submission id: ${submissionId.toString}")

    case _ => //do nothing
  }
}
