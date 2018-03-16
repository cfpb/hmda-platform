package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.institution.HmdaFiler
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, DeleteHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.{ HmdaFilerCreated, HmdaFilerDeleted }
import hmda.persistence.model.HmdaPersistentActor
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{ Subscribe, SubscribeAck }
import hmda.persistence.processing.PubSubTopics
import hmda.persistence.messages.events.pubsub.PubSubEvents.SubmissionSignedPubSub

object HmdaFilerPersistence {

  val name = "hmda-filers"

  def props: Props = Props(new HmdaFilerPersistence)

  def createHmdaFilers(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaFilerPersistence.props)
  }

  case class HmdaFilerState(filers: Set[HmdaFiler] = Set.empty[HmdaFiler]) {
    def updated(event: Event): HmdaFilerState = event match {
      case HmdaFilerCreated(hmdFiler) =>
        HmdaFilerState(filers + hmdFiler)
      case HmdaFilerDeleted(hmdaFiler) =>
        HmdaFilerState(filers - hmdaFiler)
    }
  }

}

class HmdaFilerPersistence extends HmdaPersistentActor {
  import HmdaFilerPersistence._

  override def persistenceId: String = name

  var state = HmdaFilerState()

  val mediator = DistributedPubSub(context.system).mediator

  mediator ! Subscribe(PubSubTopics.submissionSigned, self)

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {
    case SubscribeAck(Subscribe(PubSubTopics.submissionSigned, None, `self`)) =>
      log.info(s"${self.path} subscribed to ${PubSubTopics.submissionSigned}")

    case SubmissionSignedPubSub(submissionId) =>
      log.debug(s"Received Signed event for ${submissionId.toString}")
      val institutionId = submissionId.institutionId

    case CreateHmdaFiler(hmdaFiler) =>
      persist(HmdaFilerCreated(hmdaFiler)) { e =>
        updateState(e)
        sender() ! e
      }

    case DeleteHmdaFiler(hmdaFiler) =>
      val maybeFiler = state.filers.find(f => f.institutionId == hmdaFiler.institutionId)
      maybeFiler match {
        case Some(filer) =>
          persist(HmdaFilerDeleted(filer)) { e =>
            updateState(e)
            sender() ! Some(e)
          }
        case None => sender() ! None
      }

    case FindHmdaFiler(id) =>
      sender() ! state.filers.find(f => f.institutionId == id)

    case msg: Any =>
      log.warning(s"${self.path} received unknown message: ${msg.toString}")

  }
}
