package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._
import hmda.validation.rules.SourceUtils

object ValidationStats {

  case class CountLarsInSubmission() extends Command
  case class UpdateValidationStats(total: Int) extends Command
  case class ValidationStatsUpdated(total: Int) extends Event

  def props(submissionId: SubmissionId): Props = Props(new ValidationStats(submissionId))

  def createValidationStats(submissionId: SubmissionId, system: ActorSystem): ActorRef = {
    system.actorOf(ValidationStats.props(submissionId))
  }

  case class ValidationStatsState(totalLars: Int = 0) {
    def updated(event: Event): ValidationStatsState = event match {
      case ValidationStatsUpdated(total) =>
        ValidationStatsState(total)
    }
  }
}

class ValidationStats(submissionId: SubmissionId) extends HmdaPersistentActor with SourceUtils {
  import ValidationStats._

  var state = ValidationStatsState()

  override def persistenceId: String = s"ValidationStats-${submissionId.toString}"

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case CountLarsInSubmission =>
      val countF = count(events(s"HmdaFileValidator-$submissionId")
        .filter(x => x.isInstanceOf[LarValidated]))
      countF.map(count => self ! UpdateValidationStats(count))

    case UpdateValidationStats(total) =>
      persist(ValidationStatsUpdated(total)) { e =>
        updateState(e)
      }

    case GetState =>
      sender() ! state
  }

}
