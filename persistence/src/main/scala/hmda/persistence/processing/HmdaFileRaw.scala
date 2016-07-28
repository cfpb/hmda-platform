package hmda.persistence.processing

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import hmda.persistence.CommonMessages._

object HmdaFileRaw {
  def props(id: String): Props = Props(new HmdaFileRaw(id))

  def createHmdaFileRaw(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaFileRaw.props(submissionId))
  }

  case object UploadStarted extends Event
  case class AddLine(timestamp: Long, data: String) extends Command
  case object UploadCompleted extends Event
  case class LineAdded(timestamp: Long, data: String) extends Event

  // uploads is a Map of timestamp -> number of rows
  case class HmdaFileRawState(uploads: Map[Long, Int] = Map.empty) {
    def updated(event: Event): HmdaFileRawState = event match {
      case LineAdded(t, d) =>
        val updatedUploads = uploads.updated(t, uploads.getOrElse(t, 0) + 1)
        HmdaFileRawState(updatedUploads)
    }
  }

}

class HmdaFileRaw(submissionId: String) extends PersistentActor with ActorLogging {

  import HmdaFileRaw._

  override def persistenceId: String = s"HmdaFileUpload-$submissionId"

  var state = HmdaFileRawState()

  def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {
    case UploadStarted =>
      log.debug(s"Beging uploading for submission: $submissionId")
      publishEvent(UploadStarted)

    case cmd: AddLine =>
      persist(LineAdded(cmd.timestamp, cmd.data)) { e =>
        log.debug(s"Persisted: ${e.data}")
        updateState(e)
        publishEvent(e)
      }

    case UploadCompleted =>
      saveSnapshot(state)
      publishEvent(UploadCompleted)

    case GetState =>
      sender() ! state

    case Shutdown => context.stop(self)

  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: HmdaFileRawState) =>
      log.debug("Recovering from snapshot")
      state = snapshot
  }

  private def publishEvent(e: Event): Unit = {
    context.system.eventStream.publish(e)
  }

}
