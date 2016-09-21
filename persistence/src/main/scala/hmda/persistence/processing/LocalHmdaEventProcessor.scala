package hmda.persistence.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi._
import hmda.persistence.CommonMessages._
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.UpdateSubmissionStatus
import hmda.persistence.processing.HmdaFileParser._
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.HmdaRawFile.{ UploadCompleted, UploadStarted }

import scala.concurrent.duration._

object LocalHmdaEventProcessor {

  val name = "eventProcessor"

  def props(): Props = Props(new LocalHmdaEventProcessor)

  def createLocalHmdaEventProcessor(system: ActorSystem): ActorRef = {
    system.actorOf(LocalHmdaEventProcessor.props())
  }

}

class LocalHmdaEventProcessor extends Actor with ActorLogging {

  val supervisor = context.parent

  val config = ConfigFactory.load()
  val actorTimeout = config.getInt("hmda.actor-lookup-timeout")
  implicit val timeout = Timeout(actorTimeout.seconds)
  implicit val ec = context.dispatcher

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Event])
  }

  override def receive: Receive = {

    case e: Event => e match {
      case UploadStarted(submissionId) =>
        uploadStartedEvents(submissionId)

      case UploadCompleted(size, submissionId) =>
        uploadCompletedEvents(size, submissionId)

      case ParsingStarted(submissionId) =>
        parsingStartedEvents(submissionId)

      case ParsingCompletedWithErrors(submissionId) =>
        parsingCompletedWithErrorsEvents(submissionId)

      case ParsingCompleted(submissionId) =>
        parsingCompletedEvents(submissionId)

      case ValidationStarted(submissionId) =>
        validationStartedEvents(submissionId)

      case ValidationCompletedWithErrors(submissionId) =>
        validationCompletedWithErrorsEvents(submissionId)

      case ValidationCompleted(submissionId) =>
        validationCompletedEvents(submissionId)

      case _ => //ignore other events

    }
  }

  private def uploadStartedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Upload started for submission $submissionId")
    updateStatus(submissionId, Uploading)
  }

  private def uploadCompletedEvents(size: Int, submissionId: SubmissionId): Unit = {
    log.debug(s"$size lines uploaded for submission $submissionId")
    val fHmdaFileParser = (supervisor ? FindProcessingActor(HmdaFileParser.name, submissionId)).mapTo[ActorRef]
    for {
      h <- fHmdaFileParser
    } yield {
      h ! ReadHmdaRawFile(s"${HmdaRawFile.name}-$submissionId")
    }
    updateStatus(submissionId, Uploaded)
  }

  private def parsingStartedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing started for submission $submissionId")
    updateStatus(submissionId, Parsing)
  }

  private def parsingCompletedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing completed for $submissionId")
    val fHmdaFileValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
    for {
      h <- fHmdaFileValidator
    } yield {
      h ! BeginValidation
    }
    updateStatus(submissionId, Parsed)
  }

  private def parsingCompletedWithErrorsEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing completed with errors for submission $submissionId")
    updateStatus(submissionId, ParsedWithErrors)
  }

  private def validationStartedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Validation started for $submissionId")
    updateStatus(submissionId, Validating)
  }

  private def validationCompletedWithErrorsEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"validation completed with errors for submission $submissionId")
    updateStatus(submissionId, ValidatedWithErrors)
  }

  private def validationCompletedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Validation completed for submission $submissionId")
    updateStatus(submissionId, Validated)
  }

  private def updateStatus(submissionId: SubmissionId, status: SubmissionStatus): Unit = {
    val institutionId = submissionId.institutionId
    val period = submissionId.period
    val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]
    for {
      s <- fSubmissions
    } yield {
      s ! UpdateSubmissionStatus(submissionId, status)
    }
  }
}
