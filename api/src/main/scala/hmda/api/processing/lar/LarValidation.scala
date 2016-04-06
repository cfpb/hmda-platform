package hmda.api.processing.lar

import akka.actor.{ Actor, ActorLogging, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.engine.ValidationError
import hmda.validation.engine.lar.LarEngine

object LarValidation {
  def props: Props = Props(new LarValidation)

  case class CheckLar(lar: LoanApplicationRegister)
  case class CheckSyntacticalLar(lar: LoanApplicationRegister)
  case class CheckValidityLar(lar: LoanApplicationRegister)

  trait LarValidationError
  case object LarSyntacticalError extends LarValidationError
  case object LarValidityError extends LarValidationError

  trait ValidationType
  case object LarSyntactical extends ValidationType
  case object LarValidity extends ValidationType
  case object LarFull extends ValidationType

}

class LarValidation extends Actor with ActorLogging with LarEngine {
  import LarValidation._

  override def receive: Receive = {
    case CheckSyntacticalLar(lar) =>
      log.debug(s"Checking syntactical on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, LarSyntactical)
    case CheckValidityLar(lar) =>
      log.debug(s"Checking validity on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, LarValidity)
    case CheckLar(lar) =>
      log.debug(s"Checking all edits on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, LarFull)

    case _ =>
      log.error(s"Unsupported message sent to ${self.path}")
  }

  private def validationErrors(lar: LoanApplicationRegister, validationType: ValidationType): List[ValidationError] = {
    val validation = validationType match {
      case LarFull => validateLar(lar).disjunction
      case LarSyntactical => checkSyntactical(lar).disjunction
      case LarValidity => checkValidity(lar).disjunction
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
