package hmda.persistence.processing

import akka.actor.{ ActorLogging, ActorRef, ActorSelection, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.persistence.CommonMessages._
import hmda.persistence.LocalEventPublisher
import hmda.persistence.processing.HmdaFileParser.LarParsed
import hmda.validation.engine.ValidationErrors
import hmda.persistence.processing.HmdaQuery._
import hmda.persistence.processing.SingleLarValidation.CheckSyntactical
import hmda.validation.context.ValidationContext

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case object BeginValidation extends Command
  case class ValidationStarted(submissionId: String) extends Event
  case object ValidateSyntactical extends Command
  case object ValidateValidity extends Command
  case class SyntacticalValidated(submissionId: String) extends Event

  def props(id: String, larValidator: ActorSelection): Props = Props(new HmdaFileValidator(id, larValidator))

  def createHmdaFileValidator(system: ActorSystem, id: String, larValidator: ActorSelection): ActorRef = {
    system.actorOf(HmdaFileValidator.props(id, larValidator))
  }

  case class HmdaFileValidationState(
      size: Int = 0,
      syntactical: ValidationErrors = ValidationErrors.empty(),
      validity: ValidationErrors = ValidationErrors.empty(),
      quality: ValidationErrors = ValidationErrors.empty(),
      `macro`: ValidationErrors = ValidationErrors.empty()
  ) {
    def updated(event: Event): HmdaFileValidationState = {
      event match {
        case ValidationStarted(id) => HmdaFileValidationState()
        case _ => HmdaFileValidationState()
      }
    }
  }

}

class HmdaFileValidator(submissionId: String, larValidator: ActorSelection) extends PersistentActor with ActorLogging with LocalEventPublisher {

  import HmdaFileValidator._

  val parserPersistenceId = s"${HmdaFileParser.name}-$submissionId"

  implicit val system = context.system
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

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
      val event = ValidationStarted(submissionId)
      persist(event) { e =>
        updateState(e)
        publishEvent(e)
        self ! ValidateSyntactical
      }

    case ValidateSyntactical =>
      events(parserPersistenceId)
        .map { case LarParsed(lar) => lar }
        .runForeach { lar =>
          //TODO: Provide ValidationContext with Institution information
          larValidator ! CheckSyntactical(lar, ValidationContext(None))
        }
        .andThen {
          case _ =>
            self ! SyntacticalValidated(submissionId)
        }

    case errors: ValidationErrors =>
      errors.errors.foreach(error => log.info(s"error: $error"))

    case e @ SyntacticalValidated(id) =>
      persist(e) { event =>
        log.info(s"Syntactical done")
        publishEvent(event)
        self ! ValidateValidity
      }

    case ValidateValidity =>
      log.info("Starting validity checks")

    case Shutdown =>
      context stop self
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

}
