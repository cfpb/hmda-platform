package hmda.persistence.processing

import akka.actor.{ ActorRef, Props }
import hmda.model.fi.SubmissionId
import hmda.persistence.CommonMessages.Command
import hmda.persistence.HmdaActor
import hmda.persistence.processing.HmdaRawFile.AddLine
import hmda.persistence.processing.ProcessingMessages.{ CompleteUpload, StartUpload, UploadCompleted }
import hmda.persistence.processing.SubmissionFSM.Create
import hmda.persistence.processing.SubmissionManager.GetActorRef

object SubmissionManager {

  val name = "SubmissionManager"

  case class GetActorRef(name: String) extends Command

  def props(id: SubmissionId): Props = Props(new SubmissionManager(id))
}

class SubmissionManager(id: SubmissionId) extends HmdaActor {

  val submissionFSM: ActorRef = context.actorOf(SubmissionFSM.props(id))
  val submissionUpload: ActorRef = context.actorOf(HmdaRawFile.props(id))
  val submissionParser: ActorRef = context.actorOf(HmdaFileParser.props(id))
  val submissionValidator: ActorRef = context.actorOf(HmdaFileValidator.props(id))

  var uploaded: Int = 0

  override def receive: Receive = {

    case StartUpload =>
      log.info(s"Start upload for submission: ${id.toString}")
      submissionFSM ! Create
      submissionFSM ! StartUpload

    case m @ AddLine(timestamp, data) =>
      println(data)
      submissionUpload ! m

    case CompleteUpload =>
      log.info(s"Finish upload for submission: ${id.toString}")
      submissionUpload ! CompleteUpload
      submissionFSM ! CompleteUpload

    case UploadCompleted(size, submissionId) =>
      log.info(s"Completed upload for submission: ${id.toString}")
      uploaded = size
      println(uploaded)

    case GetActorRef(name) => name match {
      case SubmissionFSM.name => sender() ! submissionFSM
      case HmdaRawFile.name => sender() ! submissionUpload
      case HmdaFileParser.name => sender() ! submissionParser
      case HmdaFileValidator.name => sender() ! submissionValidator
    }

  }

}
