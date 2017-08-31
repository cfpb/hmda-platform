package hmda.validation

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.census.model.Msa
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState, Shutdown }
import hmda.persistence.messages.events.validation.ValidationStatsEvents._
import hmda.persistence.model.HmdaPersistentActor

object ValidationStats {
  def name = "ValidationStats"

  case class SubmissionStats(
    id: SubmissionId,
    totalSubmittedLars: Int = 0,
    totalValidatedLars: Int = 0,
    q070Lars: Int = 0,
    q070SoldLars: Int = 0,
    q071Lars: Int = 0,
    q071SoldLars: Int = 0,
    q072Lars: Int = 0,
    q072SoldLars: Int = 0,
    q075Ratio: Double = 0.0,
    q076Ratio: Double = 0.0,
    taxId: String = "",
    msas: Seq[Msa] = Seq[Msa]()
  )

  case class AddSubmissionSubmittedTotal(total: Int, id: SubmissionId) extends Command
  case class AddSubmissionTaxId(taxId: String, id: SubmissionId) extends Command
  case class AddSubmissionMacroStats(
    id: SubmissionId,
    total: Int,
    q070: Int,
    q070Sold: Int,
    q071: Int,
    q071Sold: Int,
    q072: Int,
    q072Sold: Int,
    q075Ratio: Double,
    q076Ratio: Double
  ) extends Command
  case class AddIrsStats(msas: Seq[Msa], id: SubmissionId) extends Command

  case class FindTotalSubmittedLars(institutionId: String, period: String) extends Command
  case class FindTotalValidatedLars(institutionId: String, period: String) extends Command
  case class FindIrsStats(submissionId: SubmissionId) extends Command
  case class FindTaxId(institutionId: String, period: String) extends Command
  case class FindQ070(institutionId: String, period: String) extends Command
  case class FindQ071(institutionId: String, period: String) extends Command
  case class FindQ072(institutionId: String, period: String) extends Command
  case class FindQ075(institutionId: String, period: String) extends Command
  case class FindQ076(institutionId: String, period: String) extends Command

  def props(): Props = Props(new ValidationStats)

  def createValidationStats(system: ActorSystem): ActorRef = {
    system.actorOf(ValidationStats.props().withDispatcher("validation-dispatcher"), "validation-stats")
  }

  case class ValidationStatsState(stats: Seq[SubmissionStats] = Nil) {
    def updated(event: Event): ValidationStatsState = event match {
      case SubmissionSubmittedTotalsAdded(total, id) =>
        val modified = getStat(id).copy(totalSubmittedLars = total)
        updateCollection(modified)
      case SubmissionMacroStatsAdded(id, total, q070, q070Sold, q071, q071Sold, q072, q072Sold, q075, q076) =>
        val modifiedSub = getStat(id).copy(
          totalValidatedLars = total,
          q070Lars = q070,
          q070SoldLars = q070Sold,
          q071Lars = q071,
          q071SoldLars = q071Sold,
          q072Lars = q072,
          q072SoldLars = q072Sold,
          q075Ratio = q075,
          q076Ratio = q076
        )
        updateCollection(modifiedSub)
      case SubmissionTaxIdAdded(tax, id) =>
        val modified = getStat(id).copy(taxId = tax)
        updateCollection(modified)
      case IrsStatsAdded(seq, id) =>
        val modified = getStat(id).copy(msas = seq)
        updateCollection(modified)
    }

    // For an institution and filing period, return SubmissionStats for latest submission
    def latestStatsFor(inst: String, period: String): SubmissionStats = {
      val filtered = stats.filter(s => s.id.institutionId == inst && s.id.period == period)
      val sorted = filtered.sortWith(_.id.sequenceNumber > _.id.sequenceNumber)
      sorted.headOption.getOrElse(SubmissionStats(SubmissionId()))
    }

    private def updateCollection(modified: SubmissionStats): ValidationStatsState = {
      ValidationStatsState(stats.filterNot(sub => sub.id == modified.id) :+ modified)
    }

    // Return SubmissionStats for a given SubmissionId. Used for updating ValidationStatsState.
    private def getStat(subId: SubmissionId): SubmissionStats = {
      stats.find(stat => stat.id == subId).getOrElse(SubmissionStats(subId))
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

  override def preStart(): Unit = {
    log.info(s"Actor started at ${self.path}")
    log.debug("Thread name for actor: " + Thread.currentThread().getName)
  }

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case AddSubmissionSubmittedTotal(total, id) =>
      persist(SubmissionSubmittedTotalsAdded(total, id)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case AddSubmissionMacroStats(id, total, q070, q070Sold, q071, q071Sold, q072, q072Sold, q075, q076) =>
      persist(SubmissionMacroStatsAdded(id, total, q070, q070Sold, q071, q071Sold, q072, q072Sold, q075, q076)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case AddSubmissionTaxId(tax, id) =>
      persist(SubmissionTaxIdAdded(tax, id)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case AddIrsStats(map, id) =>
      persist(IrsStatsAdded(map, id)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case FindTotalSubmittedLars(id, period) =>
      sender ! state.latestStatsFor(id, period).totalSubmittedLars

    case FindTotalValidatedLars(id, period) =>
      sender ! state.latestStatsFor(id, period).totalValidatedLars

    case FindIrsStats(subId) =>
      val stats = state.stats.find(s => s.id == subId).getOrElse(SubmissionStats(subId))
      sender ! stats.msas

    case FindTaxId(id, period) =>
      sender ! state.latestStatsFor(id, period).taxId

    case FindQ070(id, period) =>
      val stats = state.latestStatsFor(id, period)
      val q070Stats = (stats.q070Lars, stats.q070SoldLars)
      sender() ! q070Stats

    case FindQ071(id, period) =>
      val stats = state.latestStatsFor(id, period)
      val q071Stats = (stats.q071Lars, stats.q071SoldLars)
      sender() ! q071Stats

    case FindQ072(id, period) =>
      val stats = state.latestStatsFor(id, period)
      val q072Stats = (stats.q072Lars, stats.q072SoldLars)
      sender() ! q072Stats

    case FindQ075(id, period) =>
      val stats = state.latestStatsFor(id, period)
      sender() ! stats.q075Ratio

    case FindQ076(id, period) =>
      val stats = state.latestStatsFor(id, period)
      sender() ! stats.q076Ratio

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self
  }

}
