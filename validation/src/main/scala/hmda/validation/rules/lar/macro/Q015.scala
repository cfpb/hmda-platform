package hmda.validation.rules.lar.`macro`

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.{ ExecutionContext, Future }

object Q015 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val larsMultiplier = config.getDouble("hmda.validation.macro.Q015.numOfLarsMultiplier")
  val dollarMultiplier = config.getDouble("hmda.validation.macro.Q015.dollarAmountOfLarsMultiplier")

  override def name = "Q015"

  override def description = ""

  override def fields(lars: LoanApplicationRegisterSource) = Map(noField -> "")

  override def apply(lars: LoanApplicationRegisterSource)(implicit system: ActorSystem, materializer: ActorMaterializer, ec: ExecutionContext): Future[Result] = {

    def findAmount(lar: LoanApplicationRegister) = lar.loan.amount

    val multiFamily =
      count(lars.filter(lar => lar.loan.propertyType == 3))
    val multiFamilyAmount =
      sum(lars.filter(lar => lar.loan.propertyType == 3), findAmount)

    val totalLars = count(lars)
    val totalLarsAmount = sum(lars, findAmount)

    for {
      mt <- multiFamily
      ma <- multiFamilyAmount
      a <- totalLarsAmount
      t <- totalLars
    } yield {
      (mt.toDouble is lessThan(t * larsMultiplier)) or
        (ma.toDouble is lessThan(a * dollarMultiplier))
    }

  }
}
