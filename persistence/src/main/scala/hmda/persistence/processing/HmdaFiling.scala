package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.persistence.SnapshotOffer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState, Shutdown }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor

object HmdaFiling {

  val name = "HmdaFiling"

  case class AddLar(lar: LoanApplicationRegister) extends Command

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
    system.actorOf(HmdaFiling.props(filingPeriod).withDispatcher("persistence-dispatcher"), "hmda-filing")
  }

}

class HmdaFiling(filingPeriod: String) extends HmdaPersistentActor {
  import HmdaFiling._

  var state = HmdaFilingState()

  var counter = 0

  val config = ConfigFactory.load()

  val snapshotCounter = config.getInt("hmda.journal.snapshot.counter")

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$filingPeriod"

  override def receiveCommand: Receive = super.receiveCommand orElse {

    case LarValidated(lar, submissionId) =>
      if (counter > snapshotCounter) {
        saveSnapshot(state)
        counter = 0
      }
      persist(LarValidated(lar, submissionId)) { e =>
        counter += 1
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, s: HmdaFilingState) =>
      log.debug(s"Recovering state from snapshot for ${HmdaFiling.name}")
      state = s
    case event: Event =>
      updateState(event)
  }

}
