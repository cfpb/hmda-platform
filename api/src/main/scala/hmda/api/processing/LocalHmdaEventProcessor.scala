package hmda.api.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import hmda.model.fi._
import hmda.persistence.CommonMessages._
import hmda.persistence.institutions.SubmissionPersistence.{ UpdateSubmissionStatus, _ }
import hmda.persistence.processing.HmdaFileParser.{ ParsingCompleted, ParsingStarted, ReadHmdaRawFile }
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.HmdaRawFile.{ UploadCompleted, UploadStarted }
import hmda.persistence.processing.{ HmdaFileParser, HmdaFileValidator, HmdaRawFile }

object LocalHmdaEventProcessor {
  def props(): Props = Props(new LocalHmdaEventProcessor)

  def createLocalHmdaEventProcessor(system: ActorSystem): ActorRef = {
    system.actorOf(LocalHmdaEventProcessor.props())
  }

}

class LocalHmdaEventProcessor extends Actor with ActorLogging {

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[Event])
  }

  override def receive: Receive = {

    case e: Event => e match {
      case UploadStarted(submissionId) =>
        fireUploadStartedEvents(submissionId)

      case UploadCompleted(size, submissionId) =>
        fireUploadCompletedEvents(size, submissionId)

      case ParsingStarted(submissionId) =>
        fireParsingStartedEvents(submissionId)

      case ParsingCompleted(submissionId) =>
        fireParsingCompletedEvents(submissionId)

      case ValidationStarted(submissionId) =>
        fireValidatingEvents(submissionId)

      case ValidationCompletedWithErrors(submissionId) =>
        fireValidationCompletedWithErrorsEvents(submissionId)

      case ValidationCompleted(submissionId) =>
        fireValidationCompletedEvents(submissionId)

      case _ => //ignore other events

    }
  }

  private def fireUploadStartedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Upload started for submission $submissionId")
    updateSubmissionStatus(submissionId, Uploading)
  }

  private def fireUploadCompletedEvents(size: Int, submissionId: SubmissionId): Unit = {
    log.debug(s"$size lines uploaded for submission $submissionId")
    val hmdaFileParser = context.actorOf(HmdaFileParser.props(submissionId))
    hmdaFileParser ! ReadHmdaRawFile(s"${HmdaRawFile.name}-${submissionId.toString}")
    updateSubmissionStatus(submissionId, Uploaded)
  }

  private def fireParsingStartedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing started for submission $submissionId")
    updateSubmissionStatus(submissionId, Parsing)
  }

  private def fireParsingCompletedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing completed for $submissionId")
    val hmdaFileValidator = context.actorOf(HmdaFileValidator.props(submissionId))
    hmdaFileValidator ! BeginValidation
    updateSubmissionStatus(submissionId, Parsed)
  }

  private def fireValidatingEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Validation started for $submissionId")
    updateSubmissionStatus(submissionId, Validating)
  }

  private def fireValidationCompletedWithErrorsEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"validation completed with errors for submission $submissionId")
    updateSubmissionStatus(submissionId, ValidatedWithErrors)
  }

  private def fireValidationCompletedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Validation completed for submission $submissionId")
    updateSubmissionStatus(submissionId, Validated)
  }

  private def updateSubmissionStatus(submissionId: SubmissionId, status: SubmissionStatus) = {
    val submissions = createSubmissions(submissionId.institutionId, submissionId.period, context.system)
    submissions ! UpdateSubmissionStatus(submissionId, status)
    submissions ! Shutdown
  }

}
