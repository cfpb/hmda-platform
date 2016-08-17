package hmda.persistence.processing

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.engine.ValidationError
import hmda.validation.engine.lar.LarEngine

object SingleLarValidation {
  def props: Props = Props(new SingleLarValidation)

  case class CheckAll(lar: LoanApplicationRegister, ctx: ValidationContext)
  case class CheckSyntactical(lar: LoanApplicationRegister, ctx: ValidationContext)
  case class CheckValidity(lar: LoanApplicationRegister, ctx: ValidationContext)
  case class CheckQuality(lar: LoanApplicationRegister, ctx: ValidationContext)

  def createSingleLarValidator(system: ActorSystem): ActorRef = {
    system.actorOf(SingleLarValidation.props, "larValidation")
  }

}

class SingleLarValidation extends Actor with ActorLogging with LarEngine {
  import SingleLarValidation._

  override def receive: Receive = {
    case CheckSyntactical(lar, ctx) =>
      log.debug(s"Checking syntactical on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, ctx, checkSyntactical)
    case CheckValidity(lar, ctx) =>
      log.debug(s"Checking validity on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, ctx, checkValidity)
    case CheckQuality(lar, ctx) =>
      log.debug(s"Checking quality on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, ctx, checkQuality)
    case CheckAll(lar, ctx) =>
      log.debug(s"Checking all edits on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, ctx, validateLar)

    case _ =>
      log.error(s"Unsupported message sent to ${self.path}")
  }

  private def validationErrors(lar: LoanApplicationRegister, ctx: ValidationContext, f: (LoanApplicationRegister, ValidationContext) => LarValidation): List[ValidationError] = {
    val validation = f(lar, ctx)
    validation match {
      case scalaz.Success(_) => Nil
      case scalaz.Failure(errors) => errors.list.toList
    }
  }
}
