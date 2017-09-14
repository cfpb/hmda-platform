package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, Persisted, UploadCompleted }
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.messages.events.processing.FileUploadEvents._

object HmdaRawFile {

  val name = "HmdaRawFile"

  def props(id: SubmissionId): Props = Props(new HmdaRawFile(id))

  def createHmdaRawFile(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(HmdaRawFile.props(submissionId).withDispatcher("persistence-dispatcher"))
  }

  case class AddLine(timestamp: Long, data: String) extends Command

  case class HmdaRawFileState(size: Int = 0) {
    def updated(event: Event): HmdaRawFileState = event match {
      case LineAdded(_, _) =>
        HmdaRawFileState(size + 1)
    }
  }

}

class HmdaRawFile(submissionId: SubmissionId) extends HmdaPersistentActor {

  import HmdaRawFile._

  override def persistenceId: String = s"$name-$submissionId"

  var state = HmdaRawFileState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def receiveCommand: Receive = {

    case cmd: AddLine =>
      persist(LineAdded(cmd.timestamp, cmd.data)) { e =>
        log.debug(s"Persisted: ${e.data}")
        updateState(e)
        sender() ! Persisted
      }

    case CompleteUpload =>
      sender() ! UploadCompleted(state.size, submissionId)

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self
  }

}
