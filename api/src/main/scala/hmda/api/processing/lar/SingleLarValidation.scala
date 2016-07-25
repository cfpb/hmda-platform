package hmda.api.processing.lar

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props }
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.engine.ValidationError
import hmda.validation.engine.lar.LarEngine

object SingleLarValidation {
  def props: Props = Props(new SingleLarValidation)

  case class CheckAll(lar: LoanApplicationRegister, institution: Option[Institution])
  case class CheckSyntactical(lar: LoanApplicationRegister, institution: Option[Institution])
  case class CheckValidity(lar: LoanApplicationRegister, institution: Option[Institution])
  case class CheckQuality(lar: LoanApplicationRegister, institution: Option[Institution])

  def createSingleLarValidator(system: ActorSystem): ActorRef = {
    system.actorOf(SingleLarValidation.props, "larValidation")
  }

}

class SingleLarValidation extends Actor with ActorLogging with LarEngine {
  import SingleLarValidation._

  override def receive: Receive = {
    case CheckSyntactical(lar, institution) =>
      log.debug(s"Checking syntactical on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, institution, checkSyntactical)
    case CheckValidity(lar, institution) =>
      log.debug(s"Checking validity on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, None, checkValidity)
    case CheckQuality(lar, institution) =>
      log.debug(s"Checking quality on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, institution, checkQuality)
    case CheckAll(lar, institution) =>
      log.debug(s"Checking all edits on LAR: ${lar.toCSV}")
      sender() ! validationErrors(lar, institution, validateLar)

    case _ =>
      log.error(s"Unsupported message sent to ${self.path}")
  }

  private def validationErrors(lar: LoanApplicationRegister, institution: Option[Institution], f: (LoanApplicationRegister, Option[Institution]) => LarValidation): List[ValidationError] = {
    val validation = f(lar, institution)
    validation match {
      case scalaz.Success(_) => Nil
      case scalaz.Failure(errors) => errors.list.toList
    }
  }
}
