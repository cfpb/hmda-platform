package hmda.validation.stats

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.census.model.Msa
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages.{ Command, Shutdown }
import hmda.persistence.model.HmdaActor
import hmda.validation.messages.ValidationStatsMessages._

object ValidationStats {
  def name = "ValidationStats"

  case class AddSubmissionSubmittedTotal(total: Int, id: SubmissionId) extends Command
  case class AddSubmissionValidatedTotal(total: Int, id: SubmissionId) extends Command
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
}

class ValidationStats extends HmdaActor {
  import ValidationStats._

  var actors = Map.empty[SubmissionId, ActorRef]

  override def preStart(): Unit = {
    log.info(s"Actor started at ${self.path}")
    log.debug("Thread name for actor: " + Thread.currentThread().getName)
  }

  override def receive: Receive = {
    case AddSubmissionLarStatsActorRef(actor, submissionId) =>
      log.debug(s"Adding $submissionId to list")
      actors += submissionId -> actor

    case RemoveSubmissionLarStatsActorRef(submissionId) =>
      log.debug(s"Removing $submissionId from actor list")
      actors = actors.filterKeys(_ != submissionId)

    case msg @ AddSubmissionTaxId(_, id) =>
      val iLarStats = findLatestSubmissionActorRef(id)
      iLarStats match {
        case None => //do nothing
        case Some(larStats) => larStats forward msg
      }

    //NOTE: this is used for testing (Q130Spec)
    case msg @ AddSubmissionSubmittedTotal(_, submissionId) =>
      val tLarStats = findLatestSubmissionActorRef(submissionId)
      tLarStats match {
        case None => // Do nothing
        case Some(larStats) => larStats forward msg
      }

    //NOTE: this is used for testing (ValidationStats)
    case msg @ AddSubmissionValidatedTotal(_, submissionId) =>
      val tLarStats = findLatestSubmissionActorRef(submissionId)
      tLarStats match {
        case None => // Do nothing
        case Some(larStats) => larStats forward msg
      }

    //NOTE: this is used for testing (SubmissionIrsPathsSpec, ValidationStats, SubmissioPathsSpec)
    case msg @ AddIrsStats(_, id) =>
      val iLarStats = findLatestSubmissionActorRef(id)
      iLarStats match {
        case None => // Do nothing
        case Some(larStats) => larStats forward msg
      }

    //NOTE: this is used for testing (Q011Spec,Q070Spec,Q071Spec,Q072Spec,Q075Spec,Q076Spec)
    case msg @ AddSubmissionMacroStats(id, _, _, _, _, _, _, _, _, _) =>
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
        case None => sender() ! Seq.empty[Msa]
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindQ070(id, period) =>
      val qLarStats = findLatestSubmissionActorRef(id, period)
      qLarStats match {
        case None =>
          val empty = (0, 0)
          sender() ! empty
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindQ071(id, period) =>
      val qLarStats = findLatestSubmissionActorRef(id, period)
      qLarStats match {
        case None =>
          val empty = (0, 0)
          sender() ! empty
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindQ072(id, period) =>
      val qLarStats = findLatestSubmissionActorRef(id, period)
      qLarStats match {
        case None =>
          val empty = (0, 0)
          sender() ! empty
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindQ075(id, period) =>
      val qLarStats = findLatestSubmissionActorRef(id, period)
      qLarStats match {
        case None =>
          sender() ! 0.0
        case Some(larStats) => larStats forward msg
      }

    case msg @ FindQ076(id, period) =>
      val qLarStats = findLatestSubmissionActorRef(id, period)
      qLarStats match {
        case None =>
          sender() ! 0.0
        case Some(larStats) => larStats forward msg
      }

    case Shutdown =>
      context stop self

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

}
