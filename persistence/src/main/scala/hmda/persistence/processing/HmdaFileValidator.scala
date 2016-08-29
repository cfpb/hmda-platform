package hmda.persistence.processing

import akka.actor.{ ActorLogging, ActorRef, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.CommonMessages._
import hmda.persistence.LocalEventPublisher
import hmda.persistence.processing.HmdaFileParser.LarParsed
import hmda.persistence.processing.HmdaQuery._
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case object BeginValidation extends Command
  case class ValidationStarted(submissionId: String) extends Event
  case object CompleteSyntacticalAndValidity extends Command
  case object CompleteQuality extends Command
  case class SyntacticalAndValidityCompleted(submissionId: String) extends Event
  case class QualityCompleted(submissionId: String) extends Event
  case object CompleteValidation extends Command
  case object CompleteValidationWithErrors extends Command
  case class ValidationCompletedWithErrors(submissionId: String) extends Event
  case class ValidationCompleted(submissionId: String) extends Event
  case object ValidateLarSyntactical extends Command
  case object ValidateLarValidity extends Command
  case object ValidateLarQuality extends Command
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

class HmdaFileValidator(submissionId: String) extends PersistentActor with ActorLogging with LarEngine with LocalEventPublisher {

  import HmdaFileValidator._

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val parserPersistenceId = s"${HmdaFileParser.name}-$submissionId"

  var state = HmdaFileValidationState()

  def updateState(event: Event): Unit = {
    state = state.updated(event)
  }

  override def preStart(): Unit = {
    log.debug(s"Validation started for $submissionId")
  }

  override def postStop(): Unit = {
    log.debug(s"Validation ended for $submissionId")
  }

  override def persistenceId: String = s"$name-$submissionId"

  override def receiveCommand: Receive = {

    case BeginValidation =>
      val validationStarted = ValidationStarted(submissionId)
      publishEvent(validationStarted)
      self ! ValidateLarSyntactical

    case ValidateLarSyntactical =>
      events(parserPersistenceId)
        .map { case LarParsed(lar) => lar }
        .map(lar => checkSyntactical(lar, ValidationContext(None)).toEither)
        .map {
          case Right(lar) => lar
          case Left(errors) => ValidationErrors(errors.list.toList)
        }
        .runForeach { x =>
          self ! x
        }
        .andThen {
          case _ => self ! ValidateLarValidity
        }

    case ValidateLarValidity =>
      events(parserPersistenceId)
        .map { case LarParsed(lar) => lar }
        .map(lar => checkValidity(lar, ValidationContext(None)).toEither)
        .map {
          case Right(lar) => lar
          case Left(errors) => ValidationErrors(errors.list.toList)
        }
        .runForeach { x =>
          self ! x
        }
        .andThen {
          case _ =>
            self ! CompleteSyntacticalAndValidity
            if (state.syntactical.isEmpty && state.validity.isEmpty) {
              self ! ValidateLarQuality
            }
        }

    case ValidateLarQuality =>
      println("quality")
      events(parserPersistenceId)
        .map { case LarParsed(lar) => lar }
        .map(lar => checkQuality(lar, ValidationContext(None)).toEither)
        .map {
          case Right(lar) => lar
          case Left(errors) => ValidationErrors(errors.list.toList)
        }
        .runForeach { x =>
          self ! x
        }
        .andThen {
          case _ =>
            self ! CompleteValidation
        }

    case CompleteSyntacticalAndValidity =>
      publishEvent(SyntacticalAndValidityCompleted(submissionId))

    case CompleteValidationWithErrors =>
      publishEvent(ValidationCompletedWithErrors(submissionId))
      self ! Shutdown

    case CompleteValidation =>
      if (state.syntactical.isEmpty && state.validity.isEmpty && state.quality.isEmpty) {
        publishEvent(ValidationCompleted(submissionId))
      } else {
        publishEvent(ValidationCompletedWithErrors(submissionId))
      }

    case lar: LoanApplicationRegister =>
      persist(LarValidated(lar)) { e =>
        log.info(s"Persisted: $e")
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

    case GetState =>
      sender() ! state

    case Shutdown =>
      context stop self

  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

  private def persistErrors(errors: Seq[Event]): Unit = {
    errors.foreach { error =>
      persist(error) { e =>
        log.info(s"Persisted: ${e}")
        updateState(e)
      }
    }
  }

  private def errorsOfType(errors: Seq[ValidationError], errorType: ValidationErrorType): Seq[ValidationError] = {
    errors.filter(_.errorType == errorType)
  }

}
