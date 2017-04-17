package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.model.HmdaPersistentActor

object ValidationStats {

  def name = "ValidationStats"
  case class SubmissionStats(id: SubmissionId, totalLars: Int, taxId: String)
  case class AddSubmissionStats(stats: SubmissionStats) extends Command
  case class SubmissionStatsAdded(stats: SubmissionStats) extends Event
  case class FindTotalLars(institutionId: String, period: String) extends Command
  case class FindTaxId(institutionId: String, period: String) extends Command

  def props(): Props = Props(new ValidationStats)

  def createValidationStats(system: ActorSystem): ActorRef = {
    system.actorOf(ValidationStats.props(), "validation-stats")
  }

  case class ValidationStatsState(stats: Seq[SubmissionStats] = Nil) {
    def updated(event: Event): ValidationStatsState = event match {
      case SubmissionStatsAdded(s) =>
        val matchingSubs = stats.filter(stat =>
          stat.id.institutionId == s.id.institutionId
            && stat.id.period == s.id.period).sortWith(_.id.sequenceNumber > _.id.sequenceNumber)
        if (matchingSubs.isEmpty) {
          ValidationStatsState(stats :+ s)
        } else {
          val firstSub = matchingSubs.head
          val newSub = firstSub.copy(id = s.id, totalLars = firstSub.totalLars + s.totalLars, taxId = firstSub.taxId + s.taxId)
          ValidationStatsState(stats.filterNot(s => s == firstSub) :+ newSub)
        }
    }
  }
}

class ValidationStats extends HmdaPersistentActor {
  import ValidationStats._

  override def persistenceId: String = s"$name"

  var state = ValidationStatsState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case AddSubmissionStats(stats) =>
      persist(SubmissionStatsAdded(stats)) { e =>
        log.debug(s"Persisted: $stats")
        updateState(e)
        println(s"\nNEW STATE IS ${state.stats}")
      }
    case FindTotalLars(id, period) =>
      val submissionStats = state.stats
      val filtered = submissionStats.filter(s => s.id.institutionId == id && s.id.period == period)
      if (filtered.nonEmpty) {
        val submission = filtered.sortWith(_.id.sequenceNumber > _.id.sequenceNumber).head
        sender() ! submission.totalLars
      } else {
        sender() ! 0
      }
    case FindTaxId(id, period) =>
      val submissionStats = state.stats
      val filtered = submissionStats.filter(s => s.id.institutionId == id && s.id.period == period)
      if (filtered.nonEmpty) {
        val submission = filtered.sortWith(_.id.sequenceNumber > _.id.sequenceNumber).head
        sender() ! submission.taxId
      } else {
        sender() ! "0"
      }

    case GetState =>
      sender() ! state
  }

}
