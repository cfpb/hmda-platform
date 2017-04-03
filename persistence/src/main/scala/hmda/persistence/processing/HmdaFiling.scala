package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor

object HmdaFiling {

  val name = "HmdaFiling"

  case class AddLar(lar: LoanApplicationRegister) extends Command
  case class LarAdded(lar: LoanApplicationRegister) extends Event

  case class HmdaFilingState(filings: Map[String, Int] = Map.empty[String, Int]) {
    def updated(event: Event): HmdaFilingState = {
      event match {
        case LarValidated(_, submissionId) =>
          val count = filings.getOrElse(submissionId.toString, 0)
          HmdaFilingState(filings.updated(submissionId.toString, count + 1))
        case _ => this
      }
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

    case LarValidated(lar, submissionId) =>
      persist(LarValidated(lar, submissionId)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case GetState =>
      sender() ! state

  }

}
