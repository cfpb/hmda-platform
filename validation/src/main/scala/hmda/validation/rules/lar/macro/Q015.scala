package hmda.validation.rules.lar.`macro`

import hmda.validation._
import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.AggregateEditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.rules.lar.`macro`.MacroEditTypes._

import scala.concurrent.Future

object Q015 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val larsMultiplier = config.getDouble("hmda.validation.macro.Q015.numOfLarsMultiplier")
  val dollarMultiplier = config.getDouble("hmda.validation.macro.Q015.dollarAmountOfLarsMultiplier")

  override def name = "Q015"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

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
