package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor
import hmda.validation.ValidationStats.{ AddSubmissionMacroStats, AddSubmissionSubmittedTotal }
import hmda.validation.rules.lar.`macro`.Q071

object SubmissionLarStats {
  val name = "SubmissionStats"

  case class PersistStatsForMacroEdits() extends Command
  case class CountSubmittedLarsInSubmission() extends Command
  case class SubmittedLarsUpdated(totalSubmitted: Int) extends Event
  case class MacroStatsUpdated(totalValidated: Int, q071Total: Int, q071Sold: Int) extends Event

  def props(submissionId: SubmissionId): Props = Props(new SubmissionLarStats(submissionId))

  def createSubmissionStats(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(SubmissionLarStats.props(submissionId))
  }

  case class SubmissionLarStatsState(
      totalSubmitted: Int = 0,
      totalValidated: Int = 0,
      q071Total: Int = 0,
      q071SoldTotal: Int = 0
  ) {
    def updated(event: Event): SubmissionLarStatsState = event match {
      case SubmittedLarsUpdated(submitted) => this.copy(totalSubmitted = submitted)
      case MacroStatsUpdated(total, q071, q071sold) =>
        this.copy(totalValidated = total, q071Total = q071, q071SoldTotal = q071sold)
    }
  }
}

class SubmissionLarStats(submissionId: SubmissionId) extends HmdaPersistentActor {
  import SubmissionLarStats._

  var totalSubmittedLars = 0
  var totalValidatedLars = 0
  var q071TotalLars = 0
  var q071TotalSoldLars = 0

  var state = SubmissionLarStatsState()

  override def persistenceId: String = s"$name-${submissionId.toString}"

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case s: String =>
      totalSubmittedLars = totalSubmittedLars + 1

    case LarValidated(lar, _) =>
      totalValidatedLars = totalValidatedLars + 1
      tallyQ071Lar(lar)

    case CountSubmittedLarsInSubmission =>
      persist(SubmittedLarsUpdated(totalSubmittedLars)) { e =>
        log.debug(s"Persisted: $totalSubmittedLars")
        updateState(e)
        val validationStats = context.actorSelection("/user/validation-stats")
        validationStats ! AddSubmissionSubmittedTotal(totalSubmittedLars, submissionId)
      }

    case PersistStatsForMacroEdits =>
      persist(MacroStatsUpdated(totalValidatedLars, q071TotalLars, q071TotalSoldLars)) { e =>
        log.debug(s"Persisted: $totalValidatedLars")
        updateState(e)
        val validationStats = context.actorSelection("/user/validation-stats")
        val msg = AddSubmissionMacroStats(
          submissionId,
          totalValidatedLars,
          q071TotalLars,
          q071TotalSoldLars
        )
        validationStats ! msg
      }

    case GetState =>
      sender() ! state
  }

  private def tallyQ071Lar(lar: LoanApplicationRegister) = {
    if (Q071.relevant(lar)) {
      q071TotalLars = q071TotalLars + 1
      if (Q071.sold(lar)) {
        q071TotalSoldLars = q071TotalSoldLars + 1
      }
    }
  }

}
