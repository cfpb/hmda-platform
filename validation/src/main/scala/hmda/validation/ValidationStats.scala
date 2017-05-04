package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.model.HmdaPersistentActor

object ValidationStats {

  def name = "ValidationStats"
  case class SubmissionStats(id: SubmissionId, totalLars: Int = 0, taxId: String = "")
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
        val modified = getStat(id).copy(totalLars = total)
        updateCollection(modified)
      case SubmissionTaxIdAdded(tax, id) =>
        val modified = getStat(id).copy(taxId = tax)
        updateCollection(modified)
    }

    def latestStatsFor(inst: String, period: String): Option[SubmissionStats] = {
      val filtered = stats.filter { s =>
        s.id.institutionId == inst && s.id.period == period
      }
      if (filtered.nonEmpty) {
        val stats = filtered.sortWith(_.id.sequenceNumber > _.id.sequenceNumber).head
        Some(stats)
      } else None
    }

    private def updateCollection(modified: SubmissionStats): ValidationStatsState = {
      ValidationStatsState(stats.filterNot(sub => sub.id == modified.id) :+ modified)
    }

    private def getStat(subId: SubmissionId): SubmissionStats = {
      stats.find(stat => stat.id == subId).getOrElse(SubmissionStats(subId))
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
      state.latestStatsFor(id, period) match {
        case Some(stats) => stats.totalLars
        case None => 0
      }

    case FindTaxId(id, period) =>
      state.latestStatsFor(id, period) match {
        case Some(stats) => stats.taxId
        case None => ""
      }

    case GetState =>
      sender() ! state
  }

}
