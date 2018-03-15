package hmda.persistence.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.institution.HmdaFiler
import hmda.persistence.institutions.HmdaFilerPersistence.HmdaFilerState
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.institutions.HmdaFilerCommands.{ CreateHmdaFiler, FindHmdaFiler }
import hmda.persistence.messages.events.institutions.HmdaFilerEvents.HmdaFilerCreated
import hmda.persistence.model.HmdaPersistentActor

object HmdaFilerPersistence {
  def props: Props = Props(new HmdaFilerPersistence)

  def createHmdaFilers(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaFilerPersistence.props)
  }

  case class HmdaFilerState(filers: Set[HmdaFiler] = Set.empty[HmdaFiler]) {
    def updated(event: Event): HmdaFilerState = event match {
      case HmdaFilerCreated(hmdFiler) =>
        HmdaFilerState(filers + hmdFiler)
    }
  }

}

class HmdaFilerPersistence extends HmdaPersistentActor {

  override def persistenceId: String = "hmda-filers"

  var state = HmdaFilerState()

  override def preStart(): Unit = {
    super.preStart()
  }

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {
    case CreateHmdaFiler(hmdaFiler) =>
      persist(HmdaFilerCreated(hmdaFiler)) { e =>
        updateState(e)
        sender() ! hmdaFiler
      }

    case FindHmdaFiler(id) =>
      sender() ! state.filers.find(f => f.institutionId == id)

    case msg: Any =>
      log.warning(s"${self.path} received unknown message: ${msg.toString}")

  }
}
