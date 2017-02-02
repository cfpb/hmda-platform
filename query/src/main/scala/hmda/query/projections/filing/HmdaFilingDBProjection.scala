package hmda.query.projections.filing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import hmda.persistence.messages.CommonMessages.{ Command, Event }
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ HmdaValidatorEvent, LarValidated }
import hmda.persistence.model.HmdaActor
import hmda.query.DbConfiguration
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext

object HmdaFilingDBProjection extends FilingComponent with DbConfiguration {

  val repository = new LarRepository(config)

  case object CreateSchema extends Command
  case object DropSchema extends Command
  case class LarInserted(n: Int)
  case class FilingSchemaCreated() extends Event
  case class FilingSchemaDropped() extends Event
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
      repository.createSchema().map(_ => FilingSchemaCreated()) pipeTo sender()

    case DropSchema =>
      repository.dropSchema().map(_ => FilingSchemaDropped()) pipeTo sender()

    case event: HmdaValidatorEvent => event match {
      case LarValidated(lar) =>
        val larQuery = implicitly[LoanApplicationRegisterQuery](lar)
        val larWithPeriod = larQuery.copy(period = filingPeriod)
        log.info(s"Inserted: ${larWithPeriod.toString}")
        repository.insertOrUpdate(larWithPeriod)
          .map(x => LarInserted(x)) pipeTo sender()
    }

  }
}
