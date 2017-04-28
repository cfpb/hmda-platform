package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.model.HmdaPersistentActor

object ValidationStats {

  def name = "ValidationStats"
  case class SubmissionStats(id: SubmissionId = SubmissionId(), totalSubmittedLars: Int = 0, totalVerifiedLars: Int = 0, taxId: String = "")
  case class AddSubmissionStats(stats: SubmissionStats) extends Command
  case class SubmissionStatsAdded(stats: SubmissionStats) extends Event
  case class FindTotalSubmittedLars(institutionId: String, period: String) extends Command
  case class FindTotalVerifiedLars(institutionId: String, period: String) extends Command
  case class FindTaxId(institutionId: String, period: String) extends Command

  def props(): Props = Props(new ValidationStats)

  def createValidationStats(system: ActorSystem): ActorRef = {
    system.actorOf(ValidationStats.props(), "validation-stats")
  }

  case class ValidationStatsState(stats: Seq[SubmissionStats] = Nil) {
    def updated(event: Event): ValidationStatsState = event match {
      case SubmissionStatsAdded(s) =>
        val matchingSubs = stats.filter(stat => stat.id == s.id)
        if (matchingSubs.isEmpty) {
          ValidationStatsState(stats :+ s)
        } else {
          val oldSub = matchingSubs.head
          val newSub = oldSub.copy(
            id = s.id,
            totalSubmittedLars = oldSub.totalSubmittedLars + s.totalSubmittedLars,
            totalVerifiedLars = oldSub.totalVerifiedLars + s.totalVerifiedLars,
            taxId = oldSub.taxId + s.taxId
          )
          ValidationStatsState(stats.filterNot(s => s == oldSub) :+ newSub)
        }
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
    case AddSubmissionStats(stats) =>
      persist(SubmissionStatsAdded(stats)) { e =>
        log.debug(s"Persisted: $stats")
        updateState(e)
      }

    case FindTotalSubmittedLars(id, period) =>
      sender ! getSubmissionStat(id, period).totalSubmittedLars

    case FindTotalVerifiedLars(id, period) =>
      sender ! getSubmissionStat(id, period).totalVerifiedLars

    case FindTaxId(id, period) =>
      sender ! getSubmissionStat(id, period).taxId

    case GetState =>
      sender() ! state
  }

  private def getSubmissionStat(id: String, period: String): SubmissionStats = {
    val filtered = state.stats.filter(s => s.id.institutionId == id && s.id.period == period)
    val sorted = filtered.sortWith(_.id.sequenceNumber > _.id.sequenceNumber)
    sorted.headOption.getOrElse(SubmissionStats())
  }

}
