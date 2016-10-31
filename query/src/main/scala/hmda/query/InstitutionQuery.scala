package hmda.query

import akka.persistence.{RecoveryCompleted, SnapshotOffer}
import akka.stream.ActorMaterializer
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.{Inactive, Institution}
import hmda.model.institution.InstitutionType.Bank
import hmda.persistence.messages.CommonMessages.{Command, Event, GetState, Shutdown}
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import hmda.persistence.model.HmdaPersistentActor

object InstitutionQuery {
  case class GetInstitutionById(institutionId: String) extends Command
  case class GetInstitutionsById(ids: List[String]) extends Command
  case class LastProcessedEventOffset(seqNr: Long)
}

class InstitutionQuery extends HmdaPersistentActor {

  import InstitutionQuery._

  var inMemoryInstitutions = Set.empty[Institution]
  var offset = 0L

  override def persistenceId: String = "institutions-query"

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, LastProcessedEventOffset(seqNr)) => offset = seqNr
    case RecoveryCompleted => recoveryCompleted()
  }

  override def receiveCommand: Receive = {
    case GetInstitutionById(institutionId) =>
      val institution = inMemoryInstitutions.find(i => i.id.toString == institutionId).getOrElse(Institution("", "", Set(), CFPB, Bank, hasParent = false, status = Inactive))
      sender() ! institution

    case GetInstitutionsById(ids) =>
      val institutions = inMemoryInstitutions.filter(i => ids.contains(i.id.toString))
      sender() ! institutions

    case GetState =>
      sender() ! inMemoryInstitutions

    case Shutdown => context stop self

    case (seqNr: Long, InstitutionCreated(i)) =>
      saveSnapshot(LastProcessedEventOffset(seqNr))
      inMemoryInstitutions += i

    case (seqNr: Long, InstitutionModified(i)) =>
      saveSnapshot(LastProcessedEventOffset(seqNr))
      val others = inMemoryInstitutions.filterNot(_.id == i.id)
      inMemoryInstitutions = others + i

  }

  def recoveryCompleted(): Unit = {
    implicit val materializer = ActorMaterializer()

  }

  override def updateState(event: Event): Unit = {

  }




}
