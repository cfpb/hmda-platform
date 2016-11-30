package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.{ ExecutionContext, Future }

object Q016 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q016.numOfLarsMultiplier")
  val incomeCap = config.getInt("hmda.validation.macro.Q016.incomeCap")

  override def name = "Q016"

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    val belowIncomeThreshold = count(lars.filter(lar => lar.loan.amount < incomeCap))

    val total = count(lars)

    for {
      a <- belowIncomeThreshold
      t <- total
    } yield {
      a.toDouble is lessThanOrEqual(t * multiplier)
    }

  }
}
