package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor
import hmda.validation.ValidationStats.{ AddSubmissionStats, SubmissionStats }

object SubmissionLarStats {
  def name = "SubmissionStats"

  case class CountLarsInSubmission() extends Command
  case class SubmissionStatsUpdated(total: Int) extends Event

  def props(submissionId: SubmissionId): Props = Props(new SubmissionLarStats(submissionId))

  def createSubmissionStats(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(SubmissionLarStats.props(submissionId))
  }

  case class SubmissionLarStatsState(totalLars: Int = 0) {
    def updated(event: Event): SubmissionLarStatsState = event match {
      case SubmissionStatsUpdated(total) =>
        SubmissionLarStatsState(total)
    }
  }
}

class SubmissionLarStats(submissionId: SubmissionId) extends HmdaPersistentActor {
  import SubmissionLarStats._

  var totalLars = 0
  var state = SubmissionLarStatsState()

  override def persistenceId: String = s"$name-${submissionId.toString}"

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case LarValidated(_, _) =>
      totalLars = totalLars + 1

    case CountLarsInSubmission =>
      persist(SubmissionStatsUpdated(totalLars)) { e =>
        log.debug(s"Persisted: $totalLars")
        updateState(e)
        val validationStats = context.actorSelection("/user/validation-stats")
        validationStats ! AddSubmissionStats(SubmissionStats(submissionId, totalLars))
      }

    case GetState =>
      sender() ! state
  }

}
