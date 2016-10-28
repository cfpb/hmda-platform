package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.{ HmdaPersistentActor, LocalEventPublisher }

object HmdaRawFile {

  val name = "HmdaRawFile"

  def props(id: SubmissionId): Props = Props(new HmdaRawFile(id))

  def createHmdaRawFile(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(HmdaRawFile.props(submissionId))
  }

  case object StartUpload extends Command
  case class AddLine(timestamp: Long, data: String) extends Command
  case object CompleteUpload extends Command

  case class UploadStarted(submissionId: SubmissionId) extends Event
  case class LineAdded(timestamp: Long, data: String) extends Event
  case class UploadCompleted(size: Int, submissionId: SubmissionId) extends Event

  case class HmdaRawFileState(size: Int = 0) {
    def updated(event: Event): HmdaRawFileState = event match {
      case LineAdded(_, _) =>
        HmdaRawFileState(size + 1)
    }
  }

}

class HmdaRawFile(submissionId: SubmissionId) extends HmdaPersistentActor with LocalEventPublisher {

  import HmdaRawFile._

  override def persistenceId: String = s"$name-$submissionId"

  var state = HmdaRawFileState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {

    case StartUpload =>
      publishEvent(UploadStarted(submissionId))

    case cmd: AddLine =>
      persist(LineAdded(cmd.timestamp, cmd.data)) { e =>
        log.debug(s"Persisted: ${e.data}")
        updateState(e)
      }

    case CompleteUpload =>
      publishEvent(UploadCompleted(state.size, submissionId))

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self
  }

}
