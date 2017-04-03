package hmda.query.projections.filing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import hmda.persistence.messages.CommonMessages.{ Command, Event }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ HmdaValidatorEvent, LarValidated }
import hmda.persistence.model.HmdaActor
import hmda.query.DbConfiguration._
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext

object HmdaFilingDBProjection extends FilingComponent {

  val larRepository = new LarRepository(config)
  val larTotalMsaRepository = new LarTotalMsaRepository(config)
  val modifiedLarRepository = new ModifiedLarRepository(config)

  case object CreateSchema extends Command
  case class DeleteLars(institutionId: String)
  case class LarInserted(n: Int)
  case class FilingSchemaCreated() extends Event
  case class LarsDeleted(institutionId: String) extends Event
  def props(period: String): Props = Props(new HmdaFilingDBProjection(period))

  def createHmdaFilingDBProjection(system: ActorSystem, period: String): ActorRef = {
    system.actorOf(HmdaFilingDBProjection.props(period))
  }

}

class HmdaFilingDBProjection(filingPeriod: String) extends HmdaActor {

  implicit val ec: ExecutionContext = context.dispatcher

  import HmdaFilingDBProjection._
  import hmda.query.repository.filing.LarConverter._

  override def receive: Receive = {
    case CreateSchema =>
      val schemaCreated = for {
        s <- larRepository.createSchema()
      } yield s

      schemaCreated.map { _ =>
        larTotalMsaRepository.createSchema()
        modifiedLarRepository.createSchema()
        FilingSchemaCreated()
      } pipeTo sender()

    case DeleteLars(institutionId) =>
      larRepository.deleteByInstitutionId(institutionId)
        .map(_ => LarsDeleted(institutionId)) pipeTo sender()

    case event: HmdaValidatorEvent => event match {
      case LarValidated(lar, submissionId) =>
        val larQuery = implicitly[LoanApplicationRegisterQuery](lar)
        val larWithPeriod = larQuery.copy(period = filingPeriod, institutionId = submissionId.institutionId)
        log.debug(s"Inserted: ${larWithPeriod.toString}")
        larRepository.insertOrUpdate(larWithPeriod)
          .map(x => LarInserted(x)) pipeTo sender()
    }

  }
}
