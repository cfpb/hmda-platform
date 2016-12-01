package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes.LoanApplicationRegisterSource

import scala.concurrent.{ ExecutionContext, Future }

object Q065 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val threshold = config.getInt("hmda.validation.macro.Q065.hoepaLoanLimit")

  override def name: String = "Q065"

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val hoepaCount = count(lars.filter(lar => lar.hoepaStatus == 1))

    for {
      h <- hoepaCount
    } yield {
      h is lessThan(threshold)
    }
  }

}
