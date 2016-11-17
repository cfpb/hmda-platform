package hmda.persistence.processing

import akka.actor._
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM.FSMState
import hmda.model.fi.SubmissionStatusMessage._
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.persistence.messages.CommonMessages.{ Command, Event, GetState }
import hmda.persistence.processing.HmdaFileValidator.CompleteValidation
import hmda.persistence.processing.ProcessingMessages._
import hmda.persistence.processing.SubmissionFSM._

import scala.reflect._

object SubmissionFSM {

  val name = "SubmissionFSM"

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

  case object IRSGenerated extends SubmissionFSMState {
    override def identifier: String = iRSGeneratedMsg
  }

  case object IRSVerified extends SubmissionFSMState {
    override def identifier: String = iRSVerifiedMsg
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
  }

  case class NonEmptySubmissionData(submission: Submission) extends SubmissionData {
    override def add(s: Submission) = this
    override def update(s: Submission) = NonEmptySubmissionData(s)
    override def empty() = EmptySubmissionData
  }

  case object EmptySubmissionData extends SubmissionData {
    override def add(s: Submission) = NonEmptySubmissionData(s)
    override def update(s: Submission) = this
    override def empty() = this
  }

  def props(id: SubmissionId): Props = Props(new SubmissionFSM(id))

  def createSubmissionFSM(system: ActorSystem, id: SubmissionId): ActorRef = {
    system.actorOf(SubmissionFSM.props(id))
  }

}

class SubmissionFSM(submissionId: SubmissionId)(implicit val domainEventClassTag: ClassTag[SubmissionEvent]) extends PersistentFSM[SubmissionFSMState, SubmissionData, SubmissionEvent] {

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
  }

  startWith(Idle, EmptySubmissionData)

  when(Idle) {
    case Event(Create, _) =>
      goto(Created) applying SubmissionCreated(Submission(submissionId, hmda.model.fi.Created))

  }

  when(Created) {
    case Event(StartUpload, _) =>
      goto(Uploading) applying SubmissionUploading(Submission(submissionId, hmda.model.fi.Uploading))

  }

  when(Uploading) {
    case Event(CompleteUpload, _) =>
      goto(Uploaded) applying SubmissionUploaded(Submission(submissionId, hmda.model.fi.Uploaded))

  }

  when(Uploaded) {
    case Event(StartParsing, _) =>
      goto(Parsing) applying SubmissionParsing(Submission(submissionId, hmda.model.fi.Parsing))
  }

  when(Parsing) {
    case Event(CompleteParsing, _) =>
      goto(Parsed) applying SubmissionParsed(Submission(submissionId, hmda.model.fi.Parsed))
    case Event(CompleteParsingWithErrors, _) =>
      goto(ParsedWithErrors) applying SubmissionParsedWithErrors(Submission(submissionId, hmda.model.fi.ParsedWithErrors))
  }

  when(Parsed) {
    case Event(BeginValidation(_), _) =>
      goto(Validating) applying SubmissionValidating(Submission(submissionId, hmda.model.fi.Validating))
  }

  when(ParsedWithErrors) {
    case Event(GetState, data) =>
      stay replying data
  }

  when(Validating) {
    case Event(CompleteValidation(_), _) =>
      goto(Validated) applying SubmissionValidated(Submission(submissionId, hmda.model.fi.Validated))
    case Event(CompleteValidationWithErrors, _) =>
      goto(ValidatedWithErrors) applying SubmissionValidatedWithErrors(Submission(submissionId, hmda.model.fi.ValidatedWithErrors))
  }

  when(Validated) {
    case Event(GetState, data) =>
      stay replying data
  }

  when(ValidatedWithErrors) {
    case Event(GetState, data) =>
      stay replying data
  }

  whenUnhandled {
    case Event(GetState, data) =>
      stay replying data
    case Event(e, d) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, d)
      stay
  }

}

