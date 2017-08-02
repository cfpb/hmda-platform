package hmda.query.view.filing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import hmda.persistence.messages.CommonMessages.{ Event, GetState }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._
import hmda.query.model.ViewMessages.StreamCompleted
import hmda.query.projections.filing.HmdaFilingDBProjection
import hmda.query.projections.filing.HmdaFilingDBProjection.CreateSchema
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef

object HmdaFilingView {
  val name = "HmdaFilingView"

  def props(period: String): Props = Props(new HmdaFilingView(period))

  def createHmdaFilingView(system: ActorSystem, period: String): ActorRef = {
    system.actorOf(HmdaFilingView.props(period), s"$name-$period")
  }

  case class FilingViewState(size: Long = 0, seqNr: Long = 0L) {
    def updated(event: Event): FilingViewState = event match {
      case LarValidated(_, _) =>
        FilingViewState(size + 1, seqNr + 1)
      case _ => this
    }
  }
}

class HmdaFilingView(period: String) extends HmdaPersistentActor {

  import HmdaFilingView._

  override def persistenceId: String = s"$name-$period"

  var state = FilingViewState()

  var counter = 0

  val queryProjector = context.actorOf(HmdaFilingDBProjection.props(period), "queryProjector")

  val conf = ConfigFactory.load()
  val snapshotCounter = conf.getInt("hmda.journal.snapshot.counter")

  override def receiveCommand: Receive = super.receiveCommand orElse {
    case EventWithSeqNr(seqNr, event) =>
      if (counter >= snapshotCounter) {
        counter = 0
        saveSnapshot(state)
      }
      event match {
        case LarValidated(lar, _) =>
          log.debug(s"Reading LAR: $lar")
          updateState(event)
      }

    case GetProjectionActorRef =>
      sender() ! queryProjector

    case GetState =>
      sender() ! state
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, s: FilingViewState) => state = s
    case RecoveryCompleted => recoveryCompleted()
  }

  def recoveryCompleted(): Unit = {
    val hmdaFilingId = s"HmdaFiling-$period"
    implicit val materializer = ActorMaterializer()
    eventsWithSequenceNumber(hmdaFilingId, state.seqNr + 1, Long.MaxValue)
      .runWith(Sink.actorRef(self, StreamCompleted))
  }

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
    counter += 1
    queryProjector ! event
  }

}
