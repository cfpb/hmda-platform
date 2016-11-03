package hmda.query.projections.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.InstitutionType.Bank
import hmda.model.institution.{ Inactive, Institution }
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState, Shutdown }
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._

object InstitutionProjection {

  val name = "institutions-query"

  case class GetInstitutionById(institutionId: String) extends Command
  case class GetInstitutionsById(ids: List[String]) extends Command
  case class LastProcessedEventOffset(seqNr: Long)
  case object StreamCompleted

  def props(): Props = Props(new InstitutionProjection)

  def createInstitutionQuery(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionProjection.props(), "query-institutions")
  }

  case class InstitutionState(institutions: Set[Institution] = Set.empty[Institution], seqNr: Long = 0L) {
    def updated(event: Event): InstitutionState = {
      event match {
        case InstitutionCreated(i) =>
          InstitutionState(institutions + i, seqNr)
        case InstitutionModified(i) =>
          val others = institutions.filterNot(_.id == i.id)
          InstitutionState(others + i, seqNr)
      }
    }
  }

}

class InstitutionProjection extends HmdaPersistentActor {

  import InstitutionProjection._

  var state = InstitutionState()

  var counter = 0

  val queryProjector = context.actorOf(InstitutionQueryProjector.props)

  override def persistenceId: String = name

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, s: InstitutionState) => state = s
    case RecoveryCompleted => recoveryCompleted()
  }

  override def receiveCommand: Receive = {
    case GetInstitutionById(institutionId) =>
      val institution = state.institutions.find(i => i.id.toString == institutionId).getOrElse(Institution("", "", Set(), CFPB, Bank, hasParent = false, status = Inactive))
      sender() ! institution

    case GetInstitutionsById(ids) =>
      val institutions = state.institutions.filter(i => ids.contains(i.id.toString))
      sender() ! institutions

    case EventWithSeqNr(seqNr, event) =>
      if (counter >= 100) {
        counter = 0
        saveSnapshot(state)
      }
      event match {
        case InstitutionCreated(i) =>
          updateState(event)
        case InstitutionModified(i) =>
          updateState(event)
        case _ => //do nothing

      }

    case GetState =>
      sender() ! state.institutions

    case Shutdown => context stop self

  }

  def recoveryCompleted(): Unit = {
    implicit val materializer = ActorMaterializer()
    eventsWithSequenceNumber("institutions", state.seqNr + 1, Long.MaxValue)
      .map { e => log.debug(e.toString); e }
      .runWith(Sink.actorRef(self, StreamCompleted))
  }

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
    queryProjector ! event
  }

}
