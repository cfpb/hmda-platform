package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.model.HmdaPersistentActor

object HmdaFiling {

  val name = "hmda-filing"

  case class AddLar(lar: LoanApplicationRegister) extends Command
  //case class UpdateLar(lar: LoanApplicationRegister) extends Command
  case class LarAdded(lar: LoanApplicationRegister) extends Event
  //case class LarUpdated(lar: LoanApplicationRegister) extends Event

  case class HmdaFilingState(size: Long = 0L) {
    def updated(event: Event): HmdaFilingState = {
      HmdaFilingState(size + 1)
    }
  }

  def props(filingPeriod: String): Props = Props(new HmdaFiling(filingPeriod))

  def createHmdaFiling(system: ActorSystem, filingPeriod: String): ActorRef = {
    system.actorOf(HmdaFiling.props(filingPeriod))
  }

}

class HmdaFiling(filingPeriod: String) extends HmdaPersistentActor {
  import HmdaFiling._

  var state = HmdaFilingState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$filingPeriod"

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case AddLar(lar) =>
      persist(LarAdded(lar)) { e =>
        updateState(e)
        sender() ! LarAdded(lar)
      }
    //case UpdateLar(lar) =>
    //  persist(LarUpdated(lar)) { e =>
    //    updateState(e)
    //  }

    case GetState =>
      sender() ! state

  }

}
