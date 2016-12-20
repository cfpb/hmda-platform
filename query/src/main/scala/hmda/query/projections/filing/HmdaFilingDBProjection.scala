package hmda.query.projections.filing

import akka.actor.Actor.Receive
import akka.actor.Props
import akka.persistence.{ RecoveryCompleted, SnapshotOffer }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.model.{ HmdaActor, HmdaPersistentActor }
import hmda.persistence.processing.HmdaQuery._
import hmda.query.model.ViewMessages.StreamCompleted

object FilingDBProjection {

  val name = "filing-projection"

  case class LastProcessedEventOffset(seqNr: Long)

  def props(period: String): Props = Props(new FilingDBProjection(period))

  case class FilingDBProjectionState(size: Int = 0, seqNr: Long = 0L) {
    def updated(event: Event): FilingDBProjectionState = ???
  }
}

class FilingDBProjection(period: String) extends HmdaActor {
  override def receive: Receive = ???
}
