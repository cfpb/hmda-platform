package hmda.persistence.processing

import akka.actor.{ ActorLogging, ActorRef, ActorSelection, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.CommonMessages._
import hmda.persistence.LocalEventPublisher
import hmda.persistence.processing.HmdaFileParser.LarParsed
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.processing.SingleLarValidation._
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.validation.engine.lar.LarEngine

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case object BeginValidation extends Command
  case class ValidationStarted(submissionId: String) extends Event
  case object CompleteValidation extends Command
  case object CompleteValidationWithErrors extends Command
  case class ValidationCompletedWitErrors(submissionId: String) extends Event
  case class ValidationCompleted(submissionId: String) extends Event
  case object Validate extends Command
  case object ValidateLarSyntactical extends Command
  case object ValidateValidity extends Command
  case object ValidateQuality extends Command
  case class LarValidated(lar: LoanApplicationRegister) extends Event
  case class SyntacticalError(error: ValidationError) extends Event
  case class ValidityError(error: ValidationError) extends Event
  case class QualityError(error: ValidationError) extends Event
  case class MacroError(error: ValidationError) extends Event
  case class SyntacticalValidated(submissionId: String) extends Event

  def props(id: String, larValidator: ActorSelection): Props = Props(new HmdaFileValidator(id, larValidator))

  def createHmdaFileValidator(system: ActorSystem, id: String, larValidator: ActorSelection): ActorRef = {
    system.actorOf(HmdaFileValidator.props(id, larValidator))
  }

  case class HmdaFileValidationState(
      validSize: Int = 0,
      syntactical: Seq[ValidationError] = Nil,
      validity: Seq[ValidationError] = Nil,
      quality: Seq[ValidationError] = Nil,
      `macro`: Seq[ValidationError] = Nil
  ) {
    def updated(event: Event): HmdaFileValidationState = event match {
      case ValidationStarted(id) =>
        HmdaFileValidationState()
      case larValidated @ LarValidated(lar) =>
        HmdaFileValidationState(validSize + 1, syntactical, validity, quality)
      case SyntacticalError(e) =>
        HmdaFileValidationState(validSize, syntactical :+ e, validity, quality, `macro`)
      case ValidityError(e) =>
        HmdaFileValidationState(validSize, syntactical, validity :+ e, quality, `macro`)
      case QualityError(e) =>
        HmdaFileValidationState(validSize, syntactical, validity, quality :+ e, `macro`)
      case MacroError(e) =>
        HmdaFileValidationState(validSize, syntactical, validity, `macro` :+ e)
    }
  }
}

class HmdaFileValidator(submissionId: String, larValidator: ActorSelection) extends PersistentActor with ActorLogging with LarEngine with LocalEventPublisher {

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
      persist(validationStarted) { event =>
        publishEvent(validationStarted)
        updateState(event)
        self ! ValidateLarSyntactical
      }

    case ValidateLarSyntactical =>
      events(parserPersistenceId)
        .map { case LarParsed(lar) => lar }
        .map(lar => checkSyntactical(lar, ValidationContext(None)).toEither)
        .map {
          case Right(lar) => lar
          case Left(errors) => ValidationErrors(errors.list.toList)
        }
        //.map { x => log.info(x.toString); x }
        .runForeach { x =>
          self ! x
        }

    //    case ValidateValidity =>
    //      events(parserPersistenceId)
    //        .map { case LarParsed(lar) => lar }
    //        .runForeach { lar =>
    //          larValidator ! CheckValidity(lar, ValidationContext(None))
    //        }
    //        .andThen {
    //          case _ => self ! ValidateQuality
    //        }
    //
    //    case ValidateQuality =>
    //      events(parserPersistenceId)
    //        .map { case LarParsed(lar) => lar }
    //        .runForeach { lar =>
    //          larValidator ! CheckQuality(lar, ValidationContext(None))
    //        }
    //        .andThen {
    //          case _ => larValidator ! FinishChecks
    //       }

    case FinishChecks =>
      self ! CompleteValidation

    case CompleteValidationWithErrors =>
      publishEvent(ValidationCompletedWitErrors(submissionId))
      self ! Shutdown

    case CompleteValidation =>
      if (state.syntactical.isEmpty && state.validity.isEmpty && state.quality.isEmpty) {
        publishEvent(ValidationCompleted(submissionId))
        //self ! Shutdown
      } else {
        publishEvent(ValidationCompletedWitErrors(submissionId))
        //self ! CompleteValidationWithErrors
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
