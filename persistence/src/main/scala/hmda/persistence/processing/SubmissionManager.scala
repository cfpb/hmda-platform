package hmda.persistence.processing

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, ActorSystem, Props, ReceiveTimeout }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.persistence.messages.CommonMessages.{ Command, GetState, Shutdown }
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.HmdaFileParser.ReadHmdaRawFile
import hmda.persistence.processing.HmdaFileValidator.ValidationStarted
import hmda.persistence.processing.HmdaRawFile.AddLine
import hmda.persistence.processing.ProcessingMessages._
import hmda.persistence.processing.SubmissionFSM.{ Create, SubmissionData }
import hmda.persistence.processing.SubmissionManager.GetActorRef

import scala.concurrent.duration._

object SubmissionManager {

  val name = "SubmissionManager"

  case class GetActorRef(name: String) extends Command

  def props(submissionId: SubmissionId): Props = Props(new SubmissionManager(submissionId))

  def createSubmissionManager(system: ActorSystem, submissionId: SubmissionId): ActorRef = {
    system.actorOf(SubmissionManager.props(submissionId))
  }
}

class SubmissionManager(id: SubmissionId) extends HmdaActor {

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout").seconds
  implicit val timeout = Timeout(duration)
  implicit val ec = context.dispatcher

  val submissionFSM: ActorRef = context.actorOf(SubmissionFSM.props(id))
  val submissionUpload: ActorRef = context.actorOf(HmdaRawFile.props(id))
  val submissionParser: ActorRef = context.actorOf(HmdaFileParser.props(id))
  val submissionValidator: ActorRef = context.actorOf(HmdaFileValidator.props(id))

  var uploaded: Int = 0

  override def preStart(): Unit = {
    super.preStart()
    val config = ConfigFactory.load()
    val timeout = config.getInt("hmda.persistent-actor-timeout")
    context.setReceiveTimeout(Duration.create(timeout, TimeUnit.SECONDS))
  }

  override def receive: Receive = {

    case StartUpload =>
      log.info(s"Start upload for submission: ${id.toString}")
      submissionFSM ! Create
      submissionFSM ! StartUpload

    case m @ AddLine(timestamp, data) =>
      submissionUpload ! m

    case CompleteUpload =>
      log.info(s"Finish upload for submission: ${id.toString}")
      submissionUpload ! CompleteUpload
      submissionFSM ! CompleteUpload

    case UploadCompleted(size, submissionId) =>
      log.info(s"Completed upload for submission: ${id.toString}")
      uploaded = size
      val persistenceId = s"${HmdaRawFile.name}-$submissionId"
      submissionFSM ! StartParsing
      submissionParser ! ReadHmdaRawFile(persistenceId, self)

    case ParsingCompleted(sId) =>
      log.info(s"Completed parsing for submission: ${sId.toString}")
      submissionFSM ! CompleteParsing
      submissionValidator ! BeginValidation(self)

    case ParsingCompletedWithErrors(sId) =>
      log.info(s"Completed parsing with errors for submission: ${sId.toString}")
      submissionFSM ! CompleteParsingWithErrors

    case ValidationStarted(sId) =>
      log.info(s"Validation started for submission: ${sId.toString}")
      submissionFSM ! BeginValidation(self)

    case ValidationCompleted(sId) =>
      log.info(s"Validation completed for submission: ${sId.toString}")
      submissionFSM ! CompleteValidation(self)

    case ValidationCompletedWithErrors(sId) =>
      log.info(s"Validation completed with errors for submission: ${sId.toString}")
      submissionFSM ! CompleteValidationWithErrors

    case GetActorRef(name) => name match {
      case SubmissionFSM.name => sender() ! submissionFSM
      case HmdaRawFile.name => sender() ! submissionUpload
      case HmdaFileParser.name => sender() ! submissionParser
      case HmdaFileValidator.name => sender() ! submissionValidator
    }

    case GetState =>
      val client = sender()
      (submissionFSM ? GetState)
        .mapTo[SubmissionData]
        .map { data =>
          client ! data.get().getOrElse(Submission()).status
        }

    case ReceiveTimeout =>
      self ! Shutdown

    case Shutdown =>
      context stop self

  }

}
