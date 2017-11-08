package hmda.persistence.processing

import java.util.concurrent.TimeUnit

import akka.actor.{ ActorRef, ActorSystem, Props, ReceiveTimeout }
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Publish
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.{ Signed => _, _ }
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import hmda.persistence.messages.commands.filing.FilingCommands._
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindHmdaFiling, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence.AddSubmissionFileName
import hmda.persistence.messages.CommonMessages.{ Command, GetState, Shutdown }
import hmda.persistence.messages.events.processing.PubSubEvents.SubmissionSignedPubSub
import hmda.persistence.model.HmdaActor
import hmda.persistence.processing.HmdaFileParser.ReadHmdaRawFile
import hmda.persistence.processing.HmdaFileValidator.ValidationStarted
import hmda.persistence.processing.HmdaRawFile.AddLine
import hmda.persistence.processing.ProcessingMessages._
import hmda.persistence.processing.SubmissionFSM.{ Create, SubmissionData }
import hmda.persistence.processing.SubmissionManager.{ AddFileName, GetActorRef }
import hmda.validation.SubmissionLarStats

import scala.concurrent.Future
import scala.concurrent.duration._

object SubmissionManager {

  val name = "SubmissionManager"

  case class GetActorRef(name: String) extends Command
  case class AddFileName(fileName: String) extends Command

  def props(validationStats: ActorRef, submissionId: SubmissionId): Props = Props(new SubmissionManager(validationStats, submissionId))

  def createSubmissionManager(system: ActorSystem, validationStats: ActorRef, submissionId: SubmissionId): ActorRef = {
    system.actorOf(SubmissionManager.props(validationStats, submissionId).withDispatcher("persistence-dispatcher"))
  }
}

class SubmissionManager(validationStats: ActorRef, submissionId: SubmissionId) extends HmdaActor {

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout").seconds
  implicit val timeout = Timeout(duration)
  implicit val ec = context.dispatcher

  val period = submissionId.period
  val supervisor = context.parent
  val hmdaFilingF = (supervisor ? FindHmdaFiling(period)).mapTo[ActorRef]

  val submissionLarStats: ActorRef = context.actorOf(SubmissionLarStats.props(validationStats, submissionId)
    .withDispatcher("persistence-dispatcher"), s"submission-lar-stats-${submissionId.toString}")
  val submissionFSM: ActorRef = context.actorOf(SubmissionFSM
    .props(supervisor, submissionId)
    .withDispatcher("persistence-dispatcher"))
  val submissionUpload: ActorRef = context.actorOf(HmdaRawFile
    .props(submissionId)
    .withDispatcher("persistence-dispatcher"))
  val submissionParser: ActorRef = context.actorOf(HmdaFileParser
    .props(submissionId)
    .withDispatcher("persistence-dispatcher"))
  val submissionValidator: ActorRef = context.actorOf(HmdaFileValidator
    .props(supervisor, validationStats, submissionId)
    .withDispatcher("persistence-dispatcher"))
  val filingPersistence = (supervisor ? FindFilings(FilingPersistence.name, submissionId.institutionId)).mapTo[ActorRef]
  val submissionPersistence = (supervisor ? FindSubmissions(SubmissionPersistence.name, submissionId.institutionId, submissionId.period)).mapTo[ActorRef]

  val mediator = DistributedPubSub(context.system).mediator

  var uploaded: Int = 0

  override def preStart(): Unit = {
    super.preStart()
    val config = ConfigFactory.load()
    val timeout = config.getInt("hmda.persistent-actor-timeout")
    context.setReceiveTimeout(Duration.create(timeout, TimeUnit.SECONDS))
  }

  override def receive: Receive = {

    case AddFileName(name) =>
      submissionPersistence.map(_ ! AddSubmissionFileName(submissionId, name))

    case StartUpload =>
      log.info(s"Start upload for submission: ${submissionId.toString}")
      submissionFSM ! Create
      submissionFSM ! StartUpload
      updateFilingStatus(InProgress)

    case m @ AddLine(_, _) =>
      submissionUpload forward m

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

    case ValidationCompleted(replyTo) =>
      log.info(s"Validation completed for submission: ${submissionId.toString}")
      val result = (submissionFSM ? CompleteValidation(self)).mapTo[SubmissionStatus]
      sendResult(result, replyTo)

    case ValidationCompletedWithErrors(replyTo) =>
      log.info(s"Validation completed with errors for submission: ${submissionId.toString}")
      val result = (submissionFSM ? CompleteValidationWithErrors).mapTo[SubmissionStatus]
      sendResult(result, replyTo)

    case Signed =>
      log.info(s"Submission signed: ${submissionId.toString}")
      val result = (submissionFSM ? Sign).mapTo[Option[SubmissionStatus]]
      val originalSender: ActorRef = sender()
      result.map { r =>
        if (r.isDefined) updateFilingStatus(Completed)
        originalSender ! r
        mediator ! Publish(PubSubTopics.submissionSigned, SubmissionSignedPubSub(submissionId))
      }

    case GetActorRef(name) => name match {
      case SubmissionFSM.name => sender() ! submissionFSM
      case HmdaRawFile.name => sender() ! submissionUpload
      case HmdaFileParser.name => sender() ! submissionParser
      case HmdaFileValidator.name => sender() ! submissionValidator
      case SubmissionLarStats.name => sender() ! submissionLarStats
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

  private def sendResult[T](future: Future[T], target: Option[ActorRef]) = {
    future.map { r =>
      target match {
        case Some(actor) => actor ! r
        case _ => // Do nothing
      }
    }
  }

  private def updateFilingStatus(filingStatus: FilingStatus) = {
    filingPersistence.map(_ ? UpdateFilingStatus(period, filingStatus))
  }

}
