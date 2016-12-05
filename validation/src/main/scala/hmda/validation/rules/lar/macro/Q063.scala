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

object Q063 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q063.numOfLarsMultiplier")

  override def name: String = "Q063"

  override def description = ""

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {
    val freddieHoepaLoans =
      count(lars.filter(
        lar => {
          lar.actionTakenType == 1 &&
            lar.hoepaStatus == 1 &&
            lar.lienStatus == 1 &&
            lar.purchaserType == 3
        }
      ))

    val originated = count(lars.filter(lar => lar.actionTakenType == 1))

    for {
      f <- freddieHoepaLoans
      o <- originated
    } yield {
      f.toDouble is lessThanOrEqual(o * multiplier)
    }

  }
}
