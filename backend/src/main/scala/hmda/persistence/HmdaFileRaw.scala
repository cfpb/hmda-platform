package hmda.persistence

import akka.actor.{ ActorLogging, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.model.messages.{ ProcessingStatus, ProcessingStatusSeq }

object HmdaFileRaw {
  def props(id: String): Props = Props(new HmdaFileRaw(id))

  sealed trait Command
  sealed trait Event
  case class AddLine(timestamp: Long, data: String) extends Command
  case object CompleteUpload extends Command
  case class LineAdded(timestamp: Long, data: String) extends Event
  case object GetStatus
  case object Shutdown

  // uploads is a Map of timestamp -> number of rows
  case class HmdaFileRawState(uploads: Map[Long, Int] = Map.empty) {
    def updated(event: Event): HmdaFileRawState = event match {
      case LineAdded(t, d) =>
        val updatedUploads = uploads.updated(t, uploads.getOrElse(t, 0) + 1)
        HmdaFileRawState(updatedUploads)
    }
  }

}

class HmdaFileRaw(id: String) extends PersistentActor with ActorLogging {

  import HmdaFileRaw._

  override def persistenceId: String = s"HmdaFileRaw-$id"

  //TODO: simplify state management with an uploads counter and using ProcessingStatus directly?
  var state = HmdaFileRawState()

  var status = ProcessingStatusSeq()

  def updateState(event: Event): Unit = {
    state = state.updated(event)

  }

  override def receiveCommand: Receive = {
    case cmd: AddLine =>
      persist(LineAdded(cmd.timestamp, cmd.data)) { e =>
        log.debug(s"Persisted: ${e.data}")
        updateState(e)
        context.system.eventStream.publish(e)
      }

    case CompleteUpload =>
      saveSnapshot(state)

    case GetStatus =>
      val statusSeq = state.uploads.toSeq.map { case (l, i) => ProcessingStatus(id, l.toString, i) }
      val status = ProcessingStatusSeq(statusSeq)
      sender() ! status

    case Shutdown =>
      context.stop(self)
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: HmdaFileRawState) =>
      log.info("Recovering from snapshot")
      state = snapshot
  }

}
