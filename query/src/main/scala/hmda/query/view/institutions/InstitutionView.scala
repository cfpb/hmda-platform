package hmda.query.view.institutions

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import hmda.model.institution.Agency.CFPB
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionType.Bank
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState, Shutdown }
import hmda.persistence.messages.events.institutions.InstitutionEvents._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._
import hmda.query.DbConfiguration
import hmda.query.model.ViewMessages.StreamCompleted
import hmda.query.projections.institutions.InstitutionDBProjection

object InstitutionView {

  val name = "institutions-view"

  case class GetInstitutionById(institutionId: String) extends Command
  case class GetInstitutionsById(ids: List[String]) extends Command
  case class LastProcessedEventOffset(seqNr: Long)

  def props(): Props = Props(new InstitutionView)

  def createInstitutionView(system: ActorSystem): ActorRef = {
    system.actorOf(InstitutionView.props(), "institutions-view")
  }

  case class InstitutionViewState(institutions: Set[Institution] = Set.empty[Institution], seqNr: Long = 0L) {
    def updated(event: Event): InstitutionViewState = {
      event match {
        case InstitutionCreated(i) =>
          InstitutionViewState(institutions + i, seqNr + 1)
        case InstitutionModified(i) =>
          val others = institutions.filterNot(_.id == i.id)
          InstitutionViewState(others + i, seqNr + 1)
      }
    }
  }

}

class InstitutionView extends HmdaPersistentActor with DbConfiguration {

  import InstitutionView._

  var state = InstitutionViewState()

  var counter = 0

  val queryProjector = context.actorOf(InstitutionDBProjection.props())

  val conf = ConfigFactory.load()
  val snapshotCounter = conf.getInt("hmda.journal.snapshot.counter")

  override def persistenceId: String = name

  override def receiveCommand: Receive = {
    case GetInstitutionById(institutionId) =>
      val institution = state.institutions.find(i => i.id.toString == institutionId).getOrElse(Institution("", "", Set(), CFPB, Bank, hasParent = false))
      sender() ! institution

    case GetInstitutionsById(ids) =>
      val institutions = state.institutions.filter(i => ids.contains(i.id.toString))
      sender() ! institutions

    case EventWithSeqNr(seqNr, event) =>
      if (counter >= snapshotCounter) {
        counter = 0
        saveSnapshot(state)
      }
      event match {
        case InstitutionCreated(_) =>
          updateState(event)
        case InstitutionModified(_) =>
          updateState(event)
      }

    case GetState =>
      sender() ! state.institutions

  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, s: InstitutionViewState) => state = s
    case RecoveryCompleted => recoveryCompleted()
  }

  def recoveryCompleted(): Unit = {
    implicit val materializer = ActorMaterializer()
    eventsWithSequenceNumber("institutions", state.seqNr + 1, Long.MaxValue)
      .runWith(Sink.actorRef(self, StreamCompleted))
  }

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
    counter += 1
    queryProjector ! event
  }

}
