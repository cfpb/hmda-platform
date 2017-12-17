package hmda.validation.stats

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.census.model.Msa
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Event }
import hmda.persistence.messages.events.validation.ValidationStatsEvents._
import hmda.persistence.model.HmdaActor
import hmda.validation.messages.ValidationStatsMessages._

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

  def props(): Props = Props(new ValidationStats)

  def createValidationStats(system: ActorSystem): ActorRef = {
    system.actorOf(ValidationStats.props().withDispatcher("validation-dispatcher"), ValidationStats.name)
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

class ValidationStats extends HmdaActor {
  import ValidationStats._

  var actors = Map.empty[SubmissionId, ActorRef]

  //override def persistenceId: String = s"$name"

  //var state = ValidationStatsState()

  //  override def updateState(event: Event): Unit = {
  //    state = state.updated(event)
  //  }

  override def preStart(): Unit = {
    log.info(s"Actor started at ${self.path}")
    log.debug("Thread name for actor: " + Thread.currentThread().getName)
  }

  override def receive: Receive = {
    case AddSubmissionLarStatsActorRef(actor, submissionId) =>
      actors += submissionId -> actor

    case RemoveSubmissionLarStatsActorRef(submissionId) =>
      actors = actors.filterKeys(_ != submissionId)

    case msg @ AddSubmissionTaxId(_, id) =>
      val iLarStats = findLatestSubmissionActorRef(id)
      iLarStats match {
        case None => //do nothing
        case Some(larStats) => larStats forward msg
      }

    //NOTE: this is mostly used for testing (SubmissionIrsPathsSpec and ValidationStats)
    case msg @ AddIrsStats(_, id) =>
      val iLarStats = findLatestSubmissionActorRef(id)
      iLarStats match {
        case None => //do nothing
        case Some(larStats) => larStats forward msg
      }

    //NOTE: this is mostly used for testing (Q011Spec,Q070Spec,Q071Spec,Q072Spec,Q075Spec,Q076Spec)
    case msg @ AddSubmissionMacroStats(id, total, q070, q070Sold, q071, q071Sold, q072, q072Sold, q075, q076) =>
      val mLarStats = findLatestSubmissionActorRef(id)
      mLarStats match {
        case None => // Do nothing
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindTotalSubmittedLars(id, period) =>
      val mLarStats = findLatestSubmissionActorRef(id, period)
      mLarStats match {
        case None => sender() ! 0
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindTotalValidatedLars(id, period) =>
      val vLarStats = findLatestSubmissionActorRef(id, period)
      vLarStats match {
        case None => sender() ! 0
        case Some(larStats) =>
          larStats forward msg
      }

    case msg @ FindTaxId(id, period) =>
      val tLarStats = findLatestSubmissionActorRef(id, period)
      tLarStats match {
        case None => sender() ! ""
        case Some(larStats) =>
          larStats forward msg
      }

    case msg @ FindIrsStats(id) =>
      val iLarStats = findLatestSubmissionActorRef(id)
      iLarStats match {
        case None => sender() ! SubmissionStats(id)
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindQ070(id, period) =>
      val qLarStats = findLatestSubmissionActorRef(id, period)
      qLarStats match {
        case None =>
          println("NOT FOUND")
          val empty = (0, 0)
          sender() ! empty
        case Some(larStats) => larStats forward msg
      }

  }

  private def findLatestSubmissionActorRef(submissionId: SubmissionId): Option[ActorRef] = {
    findLatestSubmissionActorRef(submissionId.institutionId, submissionId.period)
  }

  private def findLatestSubmissionActorRef(id: String, period: String): Option[ActorRef] = {
    val sequenceNumbers = actors
      .filterKeys(sId => sId.institutionId == id && sId.period == period)
      .map(s => s._1.sequenceNumber)
    val lastSeqNr = if (sequenceNumbers.nonEmpty) sequenceNumbers.max else 0
    actors
      .find(s => s._1.sequenceNumber == lastSeqNr && s._1.period == period && s._1.institutionId == id)
      .map(_._2)
  }

  //
  //    case AddSubmissionMacroStats(id, total, q070, q070Sold, q071, q071Sold, q072, q072Sold, q075, q076) =>
  //      persist(SubmissionMacroStatsAdded(id, total, q070, q070Sold, q071, q071Sold, q072, q072Sold, q075, q076)) { e =>
  //        log.debug(s"Persisted: $e")
  //        updateState(e)
  //      }
  //
  //
  //    case FindQ070(id, period) =>
  //      val stats = state.latestStatsFor(id, period)
  //      val q070Stats = (stats.q070Lars, stats.q070SoldLars)
  //      sender() ! q070Stats
  //
  //    case FindQ071(id, period) =>
  //      val stats = state.latestStatsFor(id, period)
  //      val q071Stats = (stats.q071Lars, stats.q071SoldLars)
  //      sender() ! q071Stats
  //
  //    case FindQ072(id, period) =>
  //      val stats = state.latestStatsFor(id, period)
  //      val q072Stats = (stats.q072Lars, stats.q072SoldLars)
  //      sender() ! q072Stats
  //
  //    case FindQ075(id, period) =>
  //      val stats = state.latestStatsFor(id, period)
  //      sender() ! stats.q075Ratio
  //
  //    case FindQ076(id, period) =>
  //      val stats = state.latestStatsFor(id, period)
  //      sender() ! stats.q076Ratio
  //
  //    case GetState =>
  //      sender() ! state
  //
  //    case Shutdown =>
  //      context stop self
  //  }

}
