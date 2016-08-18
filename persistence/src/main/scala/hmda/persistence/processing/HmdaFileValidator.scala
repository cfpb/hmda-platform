package hmda.persistence.processing

import akka.actor.{ ActorLogging, ActorRef, ActorSelection, ActorSystem, Props }
import akka.persistence.PersistentActor
import akka.stream.ActorMaterializer
import hmda.persistence.CommonMessages._
import hmda.persistence.processing.HmdaFileParser.LarParsed
import hmda.persistence.processing.SingleLarValidation.CheckSyntactical
import hmda.validation.context.ValidationContext
import hmda.validation.engine._
import hmda.persistence.processing.HmdaQuery._

object HmdaFileValidator {

  val name = "HmdaFileValidator"

  case object BeginValidation extends Command
  case object ValidationStarted extends Event
  case class ValidationStarted(submissionId: String) extends Event
  case object ValidateSyntactical extends Command
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
      case ValidationStarted => HmdaFileValidationState()

    }
  }
}

class HmdaFileValidator(submissionId: String, larValidator: ActorSelection) extends PersistentActor with ActorLogging {

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
      persist(ValidationStarted) { event =>
        updateState(event)
        self ! ValidateSyntactical
        log.info(s"Validation started for submission $submissionId")
      }

    case ValidateSyntactical =>
      events(parserPersistenceId)
        .map { case LarParsed(lar) => lar }
        .runForeach { lar =>
          larValidator ! CheckSyntactical(lar, ValidationContext(None))
        }

    case validationErrors: ValidationErrors =>
      val errors = validationErrors.errors
      val syntacticalErrors = errors.filter(_.errorType == Syntactical)
      syntacticalErrors.foreach(e => println(e))
      val validityErrors = errors.filter(_.errorType == Validity)
      validityErrors.foreach(e => println(e))
      val qualityErrors = errors.filter(_.errorType == Quality)
      qualityErrors.foreach(e => println(e))

    case Shutdown =>
      context stop self

    case _ =>
  }

  override def receiveRecover: Receive = {
    case event: Event => updateState(event)
  }

}
