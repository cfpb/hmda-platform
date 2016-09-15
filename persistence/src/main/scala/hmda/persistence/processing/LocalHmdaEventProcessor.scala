package hmda.api.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.persistence.CommonMessages._
import hmda.persistence.HmdaSupervisor.FindProcessingActor
import hmda.persistence.processing.HmdaFileParser.{ ParsingCompleted, ParsingStarted, ReadHmdaRawFile }
import hmda.persistence.processing.HmdaFileValidator._
import hmda.persistence.processing.HmdaRawFile.{ UploadCompleted, UploadStarted }
import hmda.persistence.processing.{ HmdaFileParser, HmdaFileValidator, HmdaRawFile }

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
        log.debug(s"Upload started for submission $submissionId")

      case UploadCompleted(size, submissionId) =>
        fireUploadCompletedEvents(size, submissionId)

      case ParsingStarted(submissionId) =>
        fireParsingStartedEvents(submissionId)

      case ParsingCompleted(submissionId) =>
        fireParsingCompletedEvents(submissionId)

      case ValidationStarted(submissionId) =>
        log.debug(s"Validation started for $submissionId")

      case ValidationCompletedWithErrors(submissionId) =>
        log.debug(s"validation completed with errors for submission $submissionId")
        fireValidationCompletedEvents(submissionId)

      case ValidationCompleted(submissionId) =>
        fireValidationCompletedEvents(submissionId)

      case _ => //ignore other events

    }
  }

  private def fireUploadCompletedEvents(size: Int, submissionId: SubmissionId): Unit = {
    log.debug(s"$size lines uploaded for submission $submissionId")
    val fHmdaFileParser = (supervisor ? FindProcessingActor(HmdaFileParser.name, submissionId)).mapTo[ActorRef]
    for {
      h <- fHmdaFileParser
    } yield {
      h ! ReadHmdaRawFile(s"${HmdaRawFile.name}-$submissionId")
    }
  }

  private def fireParsingStartedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing started for submission $submissionId")
  }

  private def fireParsingCompletedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Parsing completed for $submissionId")
    val fHmdaFileValidator = (supervisor ? FindProcessingActor(HmdaFileValidator.name, submissionId)).mapTo[ActorRef]
    for {
      h <- fHmdaFileValidator
    } yield {
      h ! BeginValidation
    }
  }

  private def fireValidationCompletedEvents(submissionId: SubmissionId): Unit = {
    log.debug(s"Validation completed for submission $submissionId")
  }
}
