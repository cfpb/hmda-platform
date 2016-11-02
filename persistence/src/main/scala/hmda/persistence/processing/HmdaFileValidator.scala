package hmda.persistence.processing

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import akka.stream.scaladsl.{ Sink, Source }
import hmda.model.fi.SubmissionId
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.ts.TransmittalSheet
import hmda.persistence.messages.CommonMessages._
import hmda.persistence.model.{ HmdaPersistentActor, LocalEventPublisher }
import hmda.persistence.processing.HmdaFileParser.{ LarParsed, TsParsed }
import hmda.persistence.processing.HmdaQuery._
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine
import hmda.validation.engine.ts.TsEngine
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.util.Try

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case object BeginValidation extends Command
  case class ValidationStarted(submissionId: SubmissionId) extends Event
  case class ValidateMacro(source: LoanApplicationRegisterSource) extends Command
  case class CompleteMacroValidation(errors: LarValidationErrors) extends Command
  case object CompleteValidation extends Command
  case class ValidationCompletedWithErrors(submissionId: SubmissionId) extends Event
  case class ValidationCompleted(submissionId: SubmissionId) extends Event
  case class TsValidated(ts: TransmittalSheet) extends Event
  case class LarValidated(lar: LoanApplicationRegister) extends Event
  case class TsSyntacticalError(error: ValidationError) extends Event
  case class TsValidityError(error: ValidationError) extends Event
  case class TsQualityError(error: ValidationError) extends Event
  case class LarSyntacticalError(error: ValidationError) extends Event
  case class LarValidityError(error: ValidationError) extends Event
  case class LarQualityError(error: ValidationError) extends Event
  case class LarMacroError(error: ValidationError) extends Event

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
      tsMacro: Seq[ValidationError] = Nil,
      larSyntactical: Seq[ValidationError] = Nil,
      larValidity: Seq[ValidationError] = Nil,
      larQuality: Seq[ValidationError] = Nil,
      larMacro: Seq[ValidationError] = Nil
  ) {
    def updated(event: Event): HmdaFileValidationState = event match {
      case tsValidated @ TsValidated(newTs) =>
        HmdaFileValidationState(Some(newTs), lars, tsSyntactical, tsValidity, tsQuality, tsMacro, larSyntactical, larValidity, larQuality, larMacro)
      case larValidated @ LarValidated(lar) =>
        HmdaFileValidationState(ts, lars :+ lar, tsSyntactical, tsValidity, tsQuality, tsMacro, larSyntactical, larValidity, larQuality, larMacro)
      case TsSyntacticalError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical :+ e, tsValidity, tsQuality, tsMacro, larSyntactical, larValidity, larQuality, larMacro)
      case TsValidityError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity :+ e, tsQuality, tsMacro, larSyntactical, larValidity, larQuality, larMacro)
      case TsQualityError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity, tsQuality :+ e, tsMacro, larSyntactical, larValidity, larQuality, larMacro)
      case LarSyntacticalError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity, tsQuality, tsMacro, larSyntactical :+ e, larValidity, larQuality, larMacro)
      case LarValidityError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity, tsQuality, tsMacro, larSyntactical, larValidity :+ e, larQuality, larMacro)
      case LarQualityError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity, tsQuality, tsMacro, larSyntactical, larValidity, larQuality :+ e, larMacro)
      case LarMacroError(e) =>
        HmdaFileValidationState(ts, lars, tsSyntactical, tsValidity, tsQuality, tsMacro, larSyntactical, larValidity, larQuality, larMacro :+ e)
    }
  }
}

class HmdaFileValidator(submissionId: SubmissionId) extends HmdaPersistentActor with TsEngine with LarEngine with LocalEventPublisher {

  import HmdaFileValidator._

  val parserPersistenceId = s"${HmdaFileParser.name}-$submissionId"

  var state = HmdaFileValidationState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case BeginValidation =>
      val ctx = ValidationContext(None, Try(Some(submissionId.period.toInt)).getOrElse(None))
      val validationStarted = ValidationStarted(submissionId)
      publishEvent(validationStarted)
      events(parserPersistenceId)
        .filter(x => x.isInstanceOf[TsParsed])
        .map(e => e.asInstanceOf[TsParsed].ts)
        .map(ts => validateTs(ts, ctx).toEither)
        .map {
          case Right(ts) => ts
          case Left(errors) => TsValidationErrors(errors.list.toList)
        }
        .runWith(Sink.actorRef(self, NotUsed))

      val larSource: Source[LoanApplicationRegister, NotUsed] = events(parserPersistenceId)
        .filter(x => x.isInstanceOf[LarParsed])
        .map(e => e.asInstanceOf[LarParsed].lar)

      larSource.map(lar => validateLar(lar, ctx).toEither)
        .map {
          case Right(l) => l
          case Left(errors) => LarValidationErrors(errors.list.toList)
        }
        .runWith(Sink.actorRef(self, ValidateMacro(larSource)))

    case ts: TransmittalSheet =>
      persist(TsValidated(ts)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case lar: LoanApplicationRegister =>
      persist(LarValidated(lar)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case ValidateMacro(larSource) =>
      log.info("Quality Validation completed")
      val fMacro = checkMacro(larSource)
        .mapTo[LarSourceValidation]
        .map(larSourceValidation => larSourceValidation.toEither)
        .map {
          case Right(source) => CompleteValidation
          case Left(errors) => CompleteMacroValidation(LarValidationErrors(errors.list.toList))
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

    case CompleteMacroValidation(e) =>
      self ! LarValidationErrors(e.errors)
      self ! CompleteValidation

    case CompleteValidation =>
      if (state.larSyntactical.isEmpty && state.larValidity.isEmpty && state.larQuality.isEmpty && state.larMacro.isEmpty
        && state.tsSyntactical.isEmpty && state.tsValidity.isEmpty && state.tsQuality.isEmpty) {
        log.debug(s"Validation completed for $submissionId")
        publishEvent(ValidationCompleted(submissionId))
      } else {
        log.debug(s"Validation completed for $submissionId, errors found")
        publishEvent(ValidationCompletedWithErrors(submissionId))
      }

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

  private def persistErrors(errors: Seq[Event]): Unit = {
    errors.foreach { error =>
      persist(error) { e =>
        log.debug(s"Persisted: ${e}")
        updateState(e)
      }
    }
  }

  private def errorsOfType(errors: Seq[ValidationError], errorType: ValidationErrorType): Seq[ValidationError] = {
    errors.filter(_.errorType == errorType)
  }
}
