package hmda.query.projections.filing

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.pipe
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.{ HmdaValidatorEvent, LarValidated }
import hmda.persistence.model.HmdaActor
import hmda.query.DbConfiguration
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.FilingComponent

import scala.concurrent.ExecutionContext

object HmdaFilingDBProjection extends FilingComponent with DbConfiguration {

  val repository = new LarRepository(config)

  case class LarInserted(n: Int)
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
    case event: HmdaValidatorEvent => event match {
      case LarValidated(lar) =>
        val larQuery = implicitly[LoanApplicationRegisterQuery](lar)
        larQuery.copy(period = filingPeriod)
        log.info(s"Inserted: ${larQuery.toString}")
        repository.insertOrUpdate(larQuery)
          .map(x => LarInserted(x)) pipeTo sender()
    }

  }
}
