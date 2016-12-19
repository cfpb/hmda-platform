package hmda.query.view.filing

import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaQuery._
import hmda.query.model.ViewMessages.StreamCompleted

object FilingView {
  val name = "filing-view"

  trait FilingEvent extends Event

  case class FilingViewState(size: Long = 0, seqNr: Long = 0L) {
    def updated(event: Event): FilingViewState = ???
  }
}

class FilingView(period: String) extends HmdaPersistentActor {

  import FilingView._

  override def persistenceId: String = s"$name-$period"

  var state = FilingViewState()

  var counter = 0

  val conf = ConfigFactory.load()
  val snapshotCounter = conf.getInt("hmda.journal.snapshot.counter")

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
    counter += 1
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, s: FilingViewState) => state = s
    case RecoveryCompleted => recoveryCompleted()
  }

  def recoveryCompleted(): Unit = {
    implicit val materializer = ActorMaterializer()
    eventsWithSequenceNumber("filing", state.seqNr + 1, Long.MaxValue)
      .runWith(Sink.actorRef(self, StreamCompleted))
  }

}
