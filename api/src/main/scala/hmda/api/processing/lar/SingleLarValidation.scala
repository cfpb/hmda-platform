package hmda.api.processing.lar

import akka.actor.{ Actor, ActorLogging, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.engine.ValidationError
import hmda.validation.engine.lar.LarEngine

object SingleLarValidation {
  def props: Props = Props(new SingleLarValidation)

  case class CheckAll(lar: LoanApplicationRegister)
  case class CheckSyntactical(lar: LoanApplicationRegister)
  case class CheckValidity(lar: LoanApplicationRegister)

}

class SingleLarValidation extends Actor with ActorLogging with LarEngine {
  import SingleLarValidation._

  override def receive: Receive = {
    case CheckSyntactical(lar) =>
      log.debug(s"Checking syntactical on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, checkSyntactical)
    case CheckValidity(lar) =>
      log.debug(s"Checking validity on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, checkValidity)
    case CheckAll(lar) =>
      log.debug(s"Checking all edits on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, validateLar)

    case _ =>
      log.error(s"Unsupported message sent to ${self.path}")
  }

  private def validationErrors(lar: LoanApplicationRegister, f: LoanApplicationRegister => LarValidation): List[ValidationError] = {
    val validation = f(lar).disjunction
    if (validation.isRight) {
      Nil
    } else {
      val lErrors = validation.toEither.left.get
      lErrors.list.toList
    }
  }

}
