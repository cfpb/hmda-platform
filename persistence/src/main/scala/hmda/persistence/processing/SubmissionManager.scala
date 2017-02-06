package hmda.persistence.processing

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, ActorSystem, Props, ReceiveTimeout }
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.{ Filing, InProgress, Submission, SubmissionId, FilingStatus, Completed }
import hmda.persistence.institutions.FilingPersistence
import hmda.persistence.institutions.FilingPersistence.{ GetFilingByPeriod, UpdateFilingStatus }
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindHmdaFiling }
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

class SubmissionManager(submissionId: SubmissionId) extends HmdaActor {

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout").seconds
  implicit val timeout = Timeout(duration)
  implicit val ec = context.dispatcher

  val period = submissionId.period
  val supervisor = context.parent
  val hmdaFilingF = (supervisor ? FindHmdaFiling(period)).mapTo[ActorRef]

  val submissionFSM: ActorRef = context.actorOf(SubmissionFSM.props(submissionId))
  val submissionUpload: ActorRef = context.actorOf(HmdaRawFile.props(submissionId))
  val submissionParser: ActorRef = context.actorOf(HmdaFileParser.props(submissionId))
  val submissionValidator: ActorRef = context.actorOf(HmdaFileValidator.props(submissionId))
  val filingPersistence = (supervisor ? FindFilings(FilingPersistence.name, submissionId.institutionId)).mapTo[ActorRef]

  val filingF = for {
    fp <- filingPersistence
    f <- (fp ? GetFilingByPeriod(period)).mapTo[Filing]
  } yield f

  var uploaded: Int = 0

  override def preStart(): Unit = {
    super.preStart()
    val config = ConfigFactory.load()
    val timeout = config.getInt("hmda.persistent-actor-timeout")
    context.setReceiveTimeout(Duration.create(timeout, TimeUnit.SECONDS))
  }

  override def receive: Receive = {

    case StartUpload =>
      log.info(s"Start upload for submission: ${submissionId.toString}")
      submissionFSM ! Create
      submissionFSM ! StartUpload
      updateFilingStatus(InProgress)

    case m @ AddLine(_, _) =>
      submissionUpload ! m

    case CompleteUpload =>
      log.info(s"Finish upload for submission: ${submissionId.toString}")
      submissionUpload ! CompleteUpload
      submissionFSM ! CompleteUpload

    case UploadCompleted(size, sId) =>
      log.info(s"Completed upload for submission: ${sId.toString}")
      uploaded = size
      val persistenceId = s"${HmdaRawFile.name}-$sId"
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

    case Signed(sId) =>
      log.info(s"Submission signed: ${sId.toString}")
      submissionFSM ! Sign
      updateFilingStatus(Completed)

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

  private def updateFilingStatus(filingStatus: FilingStatus) = {
    for {
      p <- filingPersistence
      f <- filingF
    } yield {
      val start = if (filingStatus == InProgress) {
        System.currentTimeMillis
      } else {
        f.start
      }
      val end = if (filingStatus == Completed) {
        System.currentTimeMillis
      } else {
        f.end
      }

      p ? UpdateFilingStatus(f.copy(status = filingStatus, start = start, end = end))
    }
  }

}
