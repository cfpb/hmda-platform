package hmda.query.institutions

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

object InstitutionQuery {

  val name = "institutions-query"

  case class GetInstitutionById(institutionId: String) extends Command
  case class GetInstitutionsById(ids: List[String]) extends Command
  case class LastProcessedEventOffset(seqNr: Long)
  case object StreamCompleted

  def props(): Props = Props(new InstitutionQuery)

  def createInstitutionQuery(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionQuery.props(), "query-institutions")
  }

}

class InstitutionQuery extends HmdaPersistentActor {

  import InstitutionQuery._

  var inMemoryInstitutions = Set.empty[Institution]
  var offset = 0L

  override def persistenceId: String = name

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

    case EventWithSeqNr(seqNr, event) =>
      event match {
        case InstitutionCreated(i) =>
          saveSnapshot(LastProcessedEventOffset(seqNr))
          inMemoryInstitutions += i
          println(inMemoryInstitutions)
        case InstitutionModified(i) =>
          saveSnapshot(LastProcessedEventOffset(seqNr))
          val others = inMemoryInstitutions.filterNot(_.id == i.id)
          inMemoryInstitutions = others + i
          println(inMemoryInstitutions)
        case _ => //do nothing

      }

    case GetState =>
      sender() ! inMemoryInstitutions

    case Shutdown => context stop self

  }

  def recoveryCompleted(): Unit = {
    implicit val materializer = ActorMaterializer()
    eventsWithSequenceNumber("institutions", offset + 1, Long.MaxValue)
      .map { e => log.info(e.toString); e }
      .runWith(Sink.actorRef(self, StreamCompleted))
  }

  override def updateState(event: Event): Unit = {

  }

}
