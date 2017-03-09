package hmda.persistence.processing

import akka.actor._
import akka.pattern.ask
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionStatusMessage._
import hmda.model.fi.{ Submission, SubmissionId, SubmissionStatus }
import hmda.persistence.HmdaSupervisor.FindSubmissions
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.UpdateSubmissionStatus
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.processing.ProcessingMessages.{ Sign, _ }
import hmda.persistence.processing.SubmissionFSM.{ Signed, _ }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect._

object SubmissionFSM {

  val name = "SubmissionFSM"

  val failedMsg = "Submission status update failed"

  trait SubmissionEvent extends Event

  trait SubmissionState

  //Commands
  case object Create extends Command

  //Domain Events (persisted)
  case class SubmissionCreated(s: Submission) extends SubmissionEvent
  case class SubmissionUploading(s: Submission) extends SubmissionEvent
  case class SubmissionUploaded(s: Submission) extends SubmissionEvent

  case class SubmissionParsing(s: Submission) extends SubmissionEvent
  case class SubmissionParsed(s: Submission) extends SubmissionEvent
  case class SubmissionParsedWithErrors(s: Submission) extends SubmissionEvent
  case class SubmissionValidating(s: Submission) extends SubmissionEvent
  case class SubmissionValidated(s: Submission) extends SubmissionEvent
  case class SubmissionValidatedWithErrors(s: Submission) extends SubmissionEvent
  case class SubmissionSigned(s: Submission) extends SubmissionEvent
  case class SubmissionFailed(s: Submission) extends SubmissionEvent

  //Submission States
  sealed trait SubmissionFSMState extends FSMState

  case object Idle extends SubmissionFSMState {
    override def identifier: String = "Idle"
  }

  case object Created extends SubmissionFSMState {
    override def identifier: String = createdMsg
  }

  case object Uploading extends SubmissionFSMState {
    override def identifier: String = uploadingMsg
  }

  case object Uploaded extends SubmissionFSMState {
    override def identifier: String = uploadedMsg
  }

  case object Parsing extends SubmissionFSMState {
    override def identifier: String = parsingMsg
  }

  case object ParsedWithErrors extends SubmissionFSMState {
    override def identifier: String = parsedWithErrorsMsg
  }

  case object Parsed extends SubmissionFSMState {
    override def identifier: String = parsedMsg
  }

  case object Validating extends SubmissionFSMState {
    override def identifier: String = validatingMsg
  }

  case object ValidatedWithErrors extends SubmissionFSMState {
    override def identifier: String = validatedWithErrorsMsg
  }

  case object Validated extends SubmissionFSMState {
    override def identifier: String = validatedMsg
  }

  case object Signed extends SubmissionFSMState {
    override def identifier: String = signedMsg
  }

  case class Failed(message: String) extends SubmissionFSMState {
    override def identifier: String = message
  }

  trait SubmissionData {
    def add(s: Submission): SubmissionData
    def update(s: Submission): SubmissionData
    def empty(): SubmissionData
    def get(): Option[Submission]
  }

  case class NonEmptySubmissionData(submission: Submission) extends SubmissionData {
    override def add(s: Submission) = this
    override def update(s: Submission) = NonEmptySubmissionData(s)
    override def empty() = EmptySubmissionData
    override def get() = Some(submission)
  }

  case object EmptySubmissionData extends SubmissionData {
    override def add(s: Submission) = NonEmptySubmissionData(s)
    override def update(s: Submission) = this
    override def empty() = this
    override def get() = None
  }

  def props(id: SubmissionId): Props = Props(new SubmissionFSM(id))

  def createSubmissionFSM(system: ActorSystem, id: SubmissionId): ActorRef = {
    system.actorOf(SubmissionFSM.props(id))
  }

}

class SubmissionFSM(submissionId: SubmissionId)(implicit val domainEventClassTag: ClassTag[SubmissionEvent]) extends PersistentFSM[SubmissionFSMState, SubmissionData, SubmissionEvent] {

  val config = ConfigFactory.load()
  val actorTimeout = config.getInt("hmda.actor-lookup-timeout")
  implicit val timeout = Timeout(actorTimeout.seconds)
  implicit val ec = context.dispatcher

  val institutionId = submissionId.institutionId
  val period = submissionId.period

  val supervisor = context.actorSelection(s"/user/supervisor")
  val submissionPersistenceF = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period))
    .mapTo[ActorRef]

  override def persistenceId: String = submissionId.toString

  override def applyEvent(event: SubmissionEvent, currentData: SubmissionData): SubmissionData = event match {
    case SubmissionCreated(s) => currentData.add(s)
    case SubmissionUploading(s) => currentData.update(s)
    case SubmissionUploaded(s) => currentData.update(s)
    case SubmissionParsing(s) => currentData.update(s)
    case SubmissionParsed(s) => currentData.update(s)
    case SubmissionParsedWithErrors(s) => currentData.update(s)
    case SubmissionValidating(s) => currentData.update(s)
    case SubmissionValidated(s) => currentData.update(s)
    case SubmissionValidatedWithErrors(s) => currentData.update(s)
    case SubmissionSigned(s) => currentData.update(s)
    case SubmissionFailed(s) => currentData.update(s)
  }

  startWith(Idle, EmptySubmissionData)

  when(Idle) {
    case Event(Create, _) =>
      goto(Created) applying SubmissionCreated(Submission(submissionId, hmda.model.fi.Created))
  }

  when(Created) {
    case Event(StartUpload, _) =>
      val status = hmda.model.fi.Uploading
      updateStatus(status)
      goto(Uploading) applying SubmissionUploading(Submission(submissionId, status))
  }

  when(Uploading) {
    case Event(CompleteUpload, _) =>
      val status = hmda.model.fi.Uploaded
      updateStatus(status)
      goto(Uploaded) applying SubmissionUploaded(Submission(submissionId, status))
  }

  when(Uploaded) {
    case Event(StartParsing, _) =>
      val status = hmda.model.fi.Parsing
      updateStatus(status)
      goto(Parsing) applying SubmissionParsing(Submission(submissionId, status))
  }

  when(Parsing) {
    case Event(CompleteParsing, _) =>
      val status = hmda.model.fi.Parsed
      updateStatus(status)
      goto(Parsed) applying SubmissionParsed(Submission(submissionId, status))
    case Event(CompleteParsingWithErrors, _) =>
      val status = hmda.model.fi.ParsedWithErrors
      updateStatus(status)
      goto(ParsedWithErrors) applying SubmissionParsedWithErrors(Submission(submissionId, status))
  }

  when(Parsed) {
    case Event(BeginValidation(_), _) =>
      val status = hmda.model.fi.Validating
      updateStatus(status)
      goto(Validating) applying SubmissionValidating(Submission(submissionId, status))
  }

  when(ParsedWithErrors) {
    case Event(_, data) =>
      stay replying data
  }

  when(Validating) {
    case Event(CompleteValidation(_), _) =>
      val status = hmda.model.fi.Validated
      updateStatus(status)
      goto(Validated) applying SubmissionValidated(Submission(submissionId, status))
    case Event(CompleteValidationWithErrors, _) =>
      val status = hmda.model.fi.ValidatedWithErrors
      updateStatus(status)
      goto(ValidatedWithErrors) applying SubmissionValidatedWithErrors(Submission(submissionId, status))
  }

  when(Validated) {
    case Event(Sign, _) =>
      val status = hmda.model.fi.Signed
      updateStatus(status)
      goto(Signed) applying SubmissionSigned(Submission(submissionId, status)) replying Some(status)
  }

  when(ValidatedWithErrors) {
    case Event(Sign, _) =>
      val status = hmda.model.fi.Signed
      updateStatus(status)
      goto(Signed) applying SubmissionSigned(Submission(submissionId, status)) replying Some(status)
    case Event(GetState, data) =>
      stay replying data
  }

  when(Signed) {
    case Event(GetState, data) =>
      stay replying data
  }

  when(Failed(failedMsg)) {
    case Event(GetState, data) =>
      stay replying data
  }

  whenUnhandled {
    case Event(Sign, _) =>
      stay replying None
    case Event(Some(_), data) =>
      stay replying data
    case Event(None, _) =>
      goto(Failed(failedMsg)) applying SubmissionFailed(Submission(submissionId, hmda.model.fi.Failed(failedMsg)))
    case Event(GetState, data) =>
      stay replying data
    case Event(e, d) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, d)
      stay
  }

  private def updateStatus(status: SubmissionStatus): Future[Unit] = {
    submissionPersistenceF.map(actorRef => actorRef ! UpdateSubmissionStatus(submissionId, status))
  }

}

