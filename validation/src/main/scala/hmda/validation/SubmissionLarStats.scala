package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor
import hmda.validation.ValidationStats.{ AddSubmissionStats, SubmissionStats }

object SubmissionLarStats {
  def name = "SubmissionStats"

  case class CountVerifiedLarsInSubmission() extends Command
  case class CountSubmittedLarsInSubmission() extends Command
  case class SubmissionStatsUpdated(totalSubmitted: Int, totalVerified: Int) extends Event

  def props(submissionId: SubmissionId): Props = Props(new SubmissionLarStats(submissionId))

  def createSubmissionStats(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(SubmissionLarStats.props(submissionId))
  }

  case class SubmissionLarStatsState(totalSubmitted: Int = 0, totalVerified: Int = 0) {
    def updated(event: Event): SubmissionLarStatsState = event match {
      case SubmissionStatsUpdated(submitted, verified) =>
        SubmissionLarStatsState(totalSubmitted + submitted, totalVerified + verified)
    }
  }
}

class SubmissionLarStats(submissionId: SubmissionId) extends HmdaPersistentActor {
  import SubmissionLarStats._

  var totalSubmittedLars = 0
  var totalVerifiedLars = 0

  var state = SubmissionLarStatsState()

  override def persistenceId: String = s"$name-${submissionId.toString}"

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case s: String =>
      totalSubmittedLars = totalSubmittedLars + 1

    case LarValidated(_, _) =>
      totalVerifiedLars = totalVerifiedLars + 1

    case CountSubmittedLarsInSubmission =>
      persist(SubmissionStatsUpdated(totalSubmittedLars, 0)) { e =>
        log.debug(s"Persisted: $totalSubmittedLars")
        updateState(e)
        val validationStats = context.actorSelection("/user/validation-stats")
        validationStats ! AddSubmissionStats(SubmissionStats(submissionId, totalSubmittedLars, 0, ""))
      }

    case CountVerifiedLarsInSubmission =>
      persist(SubmissionStatsUpdated(0, totalVerifiedLars)) { e =>
        log.debug(s"Persisted: $totalVerifiedLars")
        updateState(e)
        val validationStats = context.actorSelection("/user/validation-stats")
        validationStats ! AddSubmissionStats(SubmissionStats(submissionId, 0, totalVerifiedLars, ""))
      }

    case GetState =>
      sender() ! state
  }

}
