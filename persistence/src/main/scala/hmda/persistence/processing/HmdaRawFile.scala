package hmda.persistence.processing

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence._
import hmda.persistence.CommonMessages._

object HmdaRawFile {
  def props(id: String): Props = Props(new HmdaRawFile(id))

  def createHmdaRawFile(system: ActorSystem, submissionId: String): ActorRef = {
    system.actorOf(HmdaRawFile.props(submissionId))
  }

  case class UploadStarted() extends Event
  case class AddLine(timestamp: Long, data: String) extends Command
  case class UploadCompleted() extends Event
  case class LineAdded(timestamp: Long, data: String) extends Event

  case class HmdaRawFileState(size: Int = 0) {
    def updated(event: Event): HmdaRawFileState = event match {
      case LineAdded(_, _) =>
        HmdaRawFileState(size + 1)
    }
  }

}

class HmdaRawFile(submissionId: String) extends PersistentActor with ActorLogging {

  import HmdaRawFile._

  override def persistenceId: String = s"HmdaFileUpload-$submissionId"

  var state = HmdaRawFileState()

  def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {
    case UploadStarted =>
      log.debug(s"Beging uploading for submission: $submissionId")
      publishEvent(UploadStarted())

    case cmd: AddLine =>
      persist(LineAdded(cmd.timestamp, cmd.data)) { e =>
        log.debug(s"Persisted: ${e.data}")
        updateState(e)
        publishEvent(e)
      }

    case UploadCompleted =>
      saveSnapshot(state)
      publishEvent(UploadCompleted())

    case SaveSnapshotSuccess(metadata) =>
      //only keep last snapshot
      val criteria = SnapshotSelectionCriteria(maxSequenceNr = metadata.sequenceNr - 1)
      deleteSnapshots(criteria)

    case SaveSnapshotFailure(metadata, reason) =>
      log.warning(s"Save Snapshot Failure: ${reason.getLocalizedMessage}")

    case GetState =>
      sender() ! state

    case Shutdown => context.stop(self)

  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
    case SnapshotOffer(_, snapshot: HmdaRawFileState) =>
      log.debug("Recovering from snapshot")
      state = snapshot
  }

  private def publishEvent(e: Event): Unit = {
    context.system.eventStream.publish(e)
  }

}
