package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.model.HmdaPersistentActor

object ValidationStats {

  def name = "ValidationStats"
  case class SubmissionStats(id: SubmissionId, totalLars: Int, taxId: String)
  case class AddSubmissionValidationTotal(total: Int, id: SubmissionId) extends Command
  case class AddSubmissionTaxId(taxId: String, id: SubmissionId) extends Command
  case class SubmissionValidationTotalsAdded(total: Int, id: SubmissionId) extends Event
  case class SubmissionTaxIdAdded(taxId: String, id: SubmissionId) extends Event
  case class FindTotalLars(institutionId: String, period: String) extends Command
  case class FindTaxId(institutionId: String, period: String) extends Command

  def props(): Props = Props(new ValidationStats)

  def createValidationStats(system: ActorSystem): ActorRef = {
    system.actorOf(ValidationStats.props(), "validation-stats")
  }

  case class ValidationStatsState(stats: Seq[SubmissionStats] = Nil) {
    def updated(event: Event): ValidationStatsState = event match {
      case SubmissionValidationTotalsAdded(total, id) =>
        val modifiedSub = stats.find(stat => stat.id == id)
          .getOrElse(SubmissionStats(id, 0, ""))
          .copy(totalLars = total)
        ValidationStatsState(stats.filterNot(sub => sub.id == id) :+ modifiedSub)
      case SubmissionTaxIdAdded(tax, id) =>
        val modifiedSub = stats.find(stat => stat.id == id)
          .getOrElse(SubmissionStats(id, 0, ""))
          .copy(taxId = tax)
        ValidationStatsState(stats.filterNot(sub => sub.id == id) :+ modifiedSub)
    }
  }
}

class ValidationStats extends HmdaPersistentActor {
  import ValidationStats._

  override def persistenceId: String = s"$name"

  override def preStart(): Unit = {
    log.info(s"Actor started at ${self.path}")
  }

  var state = ValidationStatsState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case AddSubmissionValidationTotal(total, id) =>
      persist(SubmissionValidationTotalsAdded(total, id)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case AddSubmissionTaxId(tax, id) =>
      persist(SubmissionTaxIdAdded(tax, id)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
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
        sender() ! ""
      }

    case GetState =>
      sender() ! state
  }

}
