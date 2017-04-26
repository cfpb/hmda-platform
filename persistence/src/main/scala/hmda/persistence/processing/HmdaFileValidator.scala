package hmda.persistence.processing

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.{ ask, pipe }
import akka.stream.scaladsl.{ Sink, Source }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.FindHmdaFiling
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.institutions.InstitutionPersistence.GetInstitution
import hmda.persistence.PaginatedResource
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.HmdaPersistentActor
import hmda.persistence.processing.HmdaFileParser.{ LarParsed, TsParsed }
import hmda.persistence.processing.ProcessingMessages.{ BeginValidation, CompleteValidation, ValidationCompleted, ValidationCompletedWithErrors }
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine
import hmda.validation.engine.ts.TsEngine
import hmda.validation.rules.lar.`macro`.MacroEditTypes._
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents._
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.validation.SubmissionLarStats
import hmda.validation.SubmissionLarStats.CountLarsInSubmission

import scala.concurrent.duration._
import scala.util.Try

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case class ValidationStarted(submissionId: SubmissionId) extends Event
  case class ValidateMacro(source: LoanApplicationRegisterSource, replyTo: ActorRef) extends Command
  case class CompleteMacroValidation(errors: LarValidationErrors, replyTo: ActorRef) extends Command
  case class VerifyEdits(editType: ValidationErrorType, verified: Boolean) extends Command
  case class TsSyntacticalError(error: ValidationError) extends Event
  case class TsValidityError(error: ValidationError) extends Event
  case class TsQualityError(error: ValidationError) extends Event
  case class LarSyntacticalError(error: ValidationError) extends Event
  case class LarValidityError(error: ValidationError) extends Event
  case class LarQualityError(error: ValidationError) extends Event
  case class LarMacroError(error: ValidationError) extends Event
  case class EditsVerified(editType: ValidationErrorType, verified: Boolean) extends Event

  case class GetNamedErrorResultsPaginated(editName: String, page: Int)

  def props(id: SubmissionId): Props = Props(new HmdaFileValidator(id))

  def createHmdaFileValidator(system: ActorSystem, id: SubmissionId): ActorRef = {
    system.actorOf(HmdaFileValidator.props(id))
  }

  case class HmdaFileValidationState(
      ts: Option[TransmittalSheet] = None,
      lars: Seq[LoanApplicationRegister] = Nil,
      tsSyntactical: Seq[ValidationError] = Nil,
      tsValidity: Seq[ValidationError] = Nil,
      tsQuality: Seq[ValidationError] = Nil,
      larSyntactical: Seq[ValidationError] = Nil,
      larValidity: Seq[ValidationError] = Nil,
      larQuality: Seq[ValidationError] = Nil,
      qualityVerified: Boolean = false,
      larMacro: Seq[ValidationError] = Vector.empty[ValidationError],
      macroVerified: Boolean = false
  ) {
    def updated(event: Event): HmdaFileValidationState = event match {
      case tsValidated @ TsValidated(newTs) => this.copy(ts = Some(newTs))
      case larValidated @ LarValidated(lar, _) => this.copy(lars = lars :+ lar)
      case TsSyntacticalError(e) => this.copy(tsSyntactical = tsSyntactical :+ e)
      case TsValidityError(e) => this.copy(tsValidity = tsValidity :+ e)
      case TsQualityError(e) => this.copy(tsQuality = tsQuality :+ e)
      case LarSyntacticalError(e) => this.copy(larSyntactical = larSyntactical :+ e)
      case LarValidityError(e) => this.copy(larValidity = larValidity :+ e)
      case LarQualityError(e) => this.copy(larQuality = larQuality :+ e)
      case LarMacroError(e) => this.copy(larMacro = larMacro :+ e)
      case EditsVerified(editType, v) =>
        if (editType == Quality) this.copy(qualityVerified = v)
        else if (editType == Macro) this.copy(macroVerified = v)
        else this
    }

    def syntacticalErrors: Seq[ValidationError] = tsSyntactical ++ larSyntactical
    def validityErrors: Seq[ValidationError] = tsValidity ++ larValidity
    def qualityErrors: Seq[ValidationError] = tsQuality ++ larQuality
    def allErrors: Seq[ValidationError] = syntacticalErrors ++ validityErrors ++ qualityErrors ++ larMacro
  }

  case class PaginatedErrors(errors: Seq[ValidationError], totalErrors: Int)
}

class HmdaFileValidator(submissionId: SubmissionId) extends HmdaPersistentActor with TsEngine with LarEngine {

  import HmdaFileValidator._

  var institution: Option[Institution] = Some(Institution.empty.copy(id = submissionId.institutionId))

  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout")

  implicit val timeout = Timeout(duration.seconds)

  val parserPersistenceId = s"${HmdaFileParser.name}-$submissionId"

  def ctx: ValidationContext = ValidationContext(institution, Try(Some(submissionId.period.toInt)).getOrElse(None))

  var state = HmdaFileValidationState()

  val supervisor = system.actorSelection("/user/supervisor")
  val fHmdaFiling = (supervisor ? FindHmdaFiling(submissionId.period)).mapTo[ActorRef]

  val submissionLarStats = context.actorOf(SubmissionLarStats.props(submissionId))

  override def preStart(): Unit = {
    super.preStart()
    val fInstitutions = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    for {
      a <- fInstitutions
      i <- (a ? GetInstitution(submissionId.institutionId)).mapTo[Option[Institution]]
    } yield {
      institution = i
    }
  }

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case BeginValidation(replyTo) =>
      val validationStarted = ValidationStarted(submissionId)
      sender() ! validationStarted
      events(parserPersistenceId)
        .filter(x => x.isInstanceOf[TsParsed])
        .map(e => e.asInstanceOf[TsParsed].ts)
        .map(ts => (ts, validateTs(ts, ctx).toEither))
        .map {
          case (_, Right(ts)) => ts
          case (ts, Left(errors)) =>
            self ! ts
            TsValidationErrors(errors.list.toList)
        }
        .runWith(Sink.actorRef(self, NotUsed))

      val larSource: Source[LoanApplicationRegister, NotUsed] = events(parserPersistenceId)
        .filter(x => x.isInstanceOf[LarParsed])
        .map(e => e.asInstanceOf[LarParsed].lar)

      larSource.map(lar => (lar, validateLar(lar, ctx).toEither))
        .map {
          case (_, Right(l)) => l
          case (lar, Left(errors)) => {
            self ! lar
            LarValidationErrors(errors.list.toList)
          }
        }
        .runWith(Sink.actorRef(self, ValidateMacro(larSource, replyTo)))

    case ts: TransmittalSheet =>
      persist(TsValidated(ts)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case lar: LoanApplicationRegister =>
      val validated = LarValidated(lar, submissionId)
      persist(validated) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
        submissionLarStats ! LarValidated(lar, submissionId)
        for {
          f <- fHmdaFiling
        } yield {
          f ! validated
        }
      }

    case ValidateMacro(larSource, replyTo) =>
      log.debug("Quality Validation completed")
      submissionLarStats ! CountLarsInSubmission
      val fMacro = checkMacro(larSource, ctx)
        .mapTo[LarSourceValidation]
        .map(larSourceValidation => larSourceValidation.toEither)
        .map {
          case Right(source) => CompleteValidation(replyTo)
          case Left(errors) => CompleteMacroValidation(LarValidationErrors(errors.list.toList), replyTo)
        }

      fMacro pipeTo self

    case tsErrors: TsValidationErrors =>
      val errors = tsErrors.errors
      val syntacticalErrors = errorsOfType(errors, Syntactical)
        .map(e => TsSyntacticalError(e))
      persistErrors(syntacticalErrors)

      val validityErrors = errorsOfType(errors, Validity)
        .map(e => TsValidityError(e))
      persistErrors(validityErrors)

      val qualityErrors = errorsOfType(errors, Quality)
        .map(e => TsQualityError(e))
      persistErrors(qualityErrors)

    case larErrors: LarValidationErrors =>
      val errors = larErrors.errors
      val syntacticalErrors = errorsOfType(errors, Syntactical)
        .map(e => LarSyntacticalError(e))
      persistErrors(syntacticalErrors)

      val validityErrors = errorsOfType(errors, Validity)
        .map(e => LarValidityError(e))
      persistErrors(validityErrors)

      val qualityErrors = errorsOfType(errors, Quality)
        .map(e => LarQualityError(e))
      persistErrors(qualityErrors)

      val macroErrors = errorsOfType(errors, Macro)
        .map(e => LarMacroError(e))
      persistErrors(macroErrors)

    case CompleteMacroValidation(e, replyTo) =>
      self ! LarValidationErrors(e.errors)
      self ! CompleteValidation(replyTo)

    case CompleteValidation(replyTo) =>
      if (state.larSyntactical.isEmpty && state.larValidity.isEmpty && state.larQuality.isEmpty && state.larMacro.isEmpty
        && state.tsSyntactical.isEmpty && state.tsValidity.isEmpty && state.tsQuality.isEmpty) {
        log.debug(s"Validation completed for $submissionId")
        replyTo ! ValidationCompleted(submissionId)
      } else {
        log.debug(s"Validation completed for $submissionId, errors found")
        replyTo ! ValidationCompletedWithErrors(submissionId)
      }

    case VerifyEdits(editType, v) =>
      if (editType == Quality || editType == Macro) {
        persist(EditsVerified(editType, v)) { e =>
          updateState(e)
          sender() ! EditsVerified(editType, v)
        }
      } else sender() ! None

    case GetState =>
      sender() ! state

    case GetNamedErrorResultsPaginated(editName, page) =>
      val allFailures = state.allErrors.filter(e => e.ruleName == editName)
      val totalSize = allFailures.size
      val p = PaginatedResource(totalSize)(page)
      val pageOfFailures = allFailures.slice(p.fromIndex, p.toIndex)
      sender() ! PaginatedErrors(pageOfFailures, totalSize)

    case Shutdown =>
      context stop self

  }

  private def persistErrors(errors: Seq[Event]): Unit = {
    errors.foreach { error =>
      persist(error) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }
    }
  }

  private def errorsOfType(errors: Seq[ValidationError], errorType: ValidationErrorType): Seq[ValidationError] = {
    errors.filter(_.errorType == errorType)
  }
}
