package hmda.persistence.processing

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.scaladsl.Sink
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._

object HmdaFiling {

  val name = "HmdaFiling"

  case class AddLar(lar: LoanApplicationRegister) extends Command
  case class LarAdded(lar: LoanApplicationRegister) extends Event

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

    case LarValidated(lar) =>
      persist(LarValidated(lar)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case GetState =>
      sender() ! state

  }

}
