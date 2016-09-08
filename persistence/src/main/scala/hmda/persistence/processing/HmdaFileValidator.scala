package hmda.persistence.processing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.scaladsl.Sink
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.CommonMessages._
import hmda.persistence.{ HmdaPersistentActor, LocalEventPublisher }
import hmda.persistence.processing.HmdaFileParser.LarParsed
import hmda.persistence.processing.HmdaQuery._
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case object BeginValidation extends Command
  case class ValidationStarted(submissionId: String) extends Event
  case object CompleteValidation extends Command
  case class ValidationCompletedWithErrors(submissionId: String) extends Event
  case class ValidationCompleted(submissionId: String) extends Event
  case class LarValidated(lar: LoanApplicationRegister) extends Event
  case class SyntacticalError(error: ValidationError) extends Event
  case class ValidityError(error: ValidationError) extends Event
  case class QualityError(error: ValidationError) extends Event

  def props(id: String): Props = Props(new HmdaFileValidator(id))

  def createHmdaFileValidator(system: ActorSystem, id: String): ActorRef = {
    system.actorOf(HmdaFileValidator.props(id))
  }

  case class HmdaFileValidationState(
      lars: Seq[LoanApplicationRegister] = Nil,
      syntactical: Seq[ValidationError] = Nil,
      validity: Seq[ValidationError] = Nil,
      quality: Seq[ValidationError] = Nil
  ) {
    def updated(event: Event): HmdaFileValidationState = event match {
      case larValidated @ LarValidated(lar) =>
        HmdaFileValidationState(lars :+ lar, syntactical, validity, quality)
      case SyntacticalError(e) =>
        HmdaFileValidationState(lars, syntactical :+ e, validity, quality)
      case ValidityError(e) =>
        HmdaFileValidationState(lars, syntactical, validity :+ e, quality)
      case QualityError(e) =>
        HmdaFileValidationState(lars, syntactical, validity, quality :+ e)

    }
  }
}

class HmdaFileValidator(submissionId: String) extends HmdaPersistentActor with LarEngine with LocalEventPublisher {

  import HmdaFileValidator._

  val parserPersistenceId = s"${HmdaFileParser.name}-$submissionId"

  var state = HmdaFileValidationState()

  override def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case BeginValidation =>
      val ctx = ValidationContext(None)
      val validationStarted = ValidationStarted(submissionId)
      publishEvent(validationStarted)
      events(parserPersistenceId)
        .filter(x => x.isInstanceOf[LarParsed])
        .map(e => e.asInstanceOf[LarParsed].lar)
        .map(lar => validateLar(lar, ctx).toEither)
        .map {
          case Right(l) => l
          case Left(errors) => ValidationErrors(errors.list.toList)
        }
        .runWith(Sink.actorRef(self, CompleteValidation))

    case lar: LoanApplicationRegister =>
      persist(LarValidated(lar)) { e =>
        log.debug(s"Persisted: $e")
        updateState(e)
      }

    case validationErrors: ValidationErrors =>
      val errors = validationErrors.errors
      val syntacticalErrors = errorsOfType(errors, Syntactical)
        .map(e => SyntacticalError(e))
      persistErrors(syntacticalErrors)

      val validityErrors = errorsOfType(errors, Validity)
        .map(e => ValidityError(e))
      persistErrors(validityErrors)

      val qualityErrors = errorsOfType(errors, Quality)
        .map(e => QualityError(e))
      persistErrors(qualityErrors)

    case CompleteValidation =>
      if (state.syntactical.isEmpty && state.validity.isEmpty && state.quality.isEmpty) {
        publishEvent(ValidationCompleted(submissionId))
      } else {
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
