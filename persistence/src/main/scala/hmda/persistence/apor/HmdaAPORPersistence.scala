package hmda.persistence.apor

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.apor.{ APOR, FixedRate, VariableRate }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import hmda.persistence.model.HmdaPersistentActor

object HmdaAPORPersistence {
  val name = "hmda-apor-persistence"

  def props(): Props = Props(new HmdaAPORPersistence)
  def createAPORPersistence(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaAPORPersistence.props(), name)
  }

  case class HmdaAPORState(fixedRate: List[APOR] = Nil, variableRate: List[APOR] = Nil) {
    def update(event: Event): HmdaAPORState = event match {
      case AporCreated(apor, rateType) => rateType match {
        case FixedRate => HmdaAPORState(apor :: fixedRate, variableRate)
        case VariableRate => HmdaAPORState(fixedRate, apor :: variableRate)
      }
    }
  }
}

class HmdaAPORPersistence extends HmdaPersistentActor {
  import HmdaAPORPersistence._

  var state = HmdaAPORState()

  override def persistenceId: String = s"$name"

  override def updateState(event: Event): Unit =
    state = state.update(event)

  override def receiveCommand: Receive = {
    case CreateApor(apor, rateType) =>
      persist(AporCreated(apor, rateType)) { e =>
        updateState(e)
        sender() ! e
      }

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

}
