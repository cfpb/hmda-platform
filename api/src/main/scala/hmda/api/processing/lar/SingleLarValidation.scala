package hmda.api.processing.lar

import akka.actor.{ Actor, ActorLogging, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.engine.ValidationError
import hmda.validation.engine.lar.LarEngine

object SingleLarValidation {
  def props: Props = Props(new SingleLarValidation)

  case class CheckLar(lar: LoanApplicationRegister)
  case class CheckSyntacticalLar(lar: LoanApplicationRegister)
  case class CheckValidityLar(lar: LoanApplicationRegister)

  trait LarValidationError
  case object LarSyntacticalError extends LarValidationError
  case object LarValidityError extends LarValidationError

  trait ValidationType
  case object LarSyntacticalValidation extends ValidationType
  case object LarValidityValidation extends ValidationType
  case object LarFullValidation extends ValidationType

}

class SingleLarValidation extends Actor with ActorLogging with LarEngine {
  import SingleLarValidation._

  override def receive: Receive = {
    case CheckSyntacticalLar(lar) =>
      log.debug(s"Checking syntactical on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, LarSyntacticalValidation)
    case CheckValidityLar(lar) =>
      log.debug(s"Checking validity on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, LarValidityValidation)
    case CheckLar(lar) =>
      log.debug(s"Checking all edits on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, LarFullValidation)

    case _ =>
      log.error(s"Unsupported message sent to ${self.path}")
  }

  private def validationErrors(lar: LoanApplicationRegister, validationType: ValidationType): List[ValidationError] = {
    val validation = validationType match {
      case LarFullValidation => validateLar(lar).disjunction
      case LarSyntacticalValidation => checkSyntactical(lar).disjunction
      case LarValidityValidation => checkValidity(lar).disjunction
    }
    if (validation.isRight) {
      Nil
    } else {
      val lErrors = validation.toEither.left.get
      val errors = lErrors.head :: lErrors.tail.toList
      errors
    }
  }

}
