package hmda.persistence

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.persistence.CommonMessages._

object HmdaFileUpload {
  def props(id: String): Props = Props(new HmdaFileUpload(id))

  def createHmdaFileUpload(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaFileUpload.props(submissionId))
  }

  case class AddLine(timestamp: Long, data: String) extends Command
  case object CompleteUpload extends Command
  case class LineAdded(timestamp: Long, data: String) extends Event

  // uploads is a Map of timestamp -> number of rows
  case class HmdaFileUploadState(uploads: Map[Long, Int] = Map.empty) {
    def updated(event: Event): HmdaFileUploadState = event match {
      case LineAdded(t, d) =>
        val updatedUploads = uploads.updated(t, uploads.getOrElse(t, 0) + 1)
        HmdaFileUploadState(updatedUploads)
    }
  }

}

class HmdaFileUpload(submissionId: String) extends PersistentActor with ActorLogging {

  import HmdaFileUpload._

  override def persistenceId: String = s"HmdaFileUpload-$submissionId"

  var state = HmdaFileUploadState()

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

    case CompleteUpload => saveSnapshot(state)

    case GetState =>
      sender() ! state

    case Shutdown => context.stop(self)

  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: HmdaFileUploadState) =>
      log.debug("Recovering from snapshot")
      state = snapshot
  }

}
