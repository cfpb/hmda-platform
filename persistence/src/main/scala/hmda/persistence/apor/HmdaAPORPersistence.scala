package hmda.persistence.apor

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.apor.{ APOR, FixedRate, RateType, VariableRate }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.HmdaPersistentActor

object HmdaAPORPersistence {
  val name = "hmda-apor-persistence"

  def props(): Props = Props(new HmdaAPORPersistence)
  def createAPORPersistence(system: ActorSystem): ActorRef = {
    system.actorOf(HmdaAPORPersistence.props(), name)
  }

  case class CreateApor(apor: APOR, rateType: RateType) extends Command
  case class AporCreated(apor: APOR, rateType: RateType) extends Event

  case class HmdaAPORState(fixedRate: Seq[APOR] = Nil, variableRate: Seq[APOR] = Nil) {
    def update(event: Event): HmdaAPORState = event match {
      case AporCreated(apor, rateType) => rateType match {
        case FixedRate => HmdaAPORState(fixedRate :+ apor, variableRate)
        case VariableRate => HmdaAPORState(fixedRate, variableRate :+ apor)
      }
    }
  }
}

class HmdaAPORPersistence extends HmdaPersistentActor {
  import HmdaAPORPersistence._

  var state = HmdaAPORState()

  override def persistenceId: String = s"$name"

  override def updateState(event: Event): Unit =
    state.update(event)

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
