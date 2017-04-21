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

object Q006 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val multiplier = config.getDouble("hmda.validation.macro.Q006.numOfLarsMultiplier")
  val numOfOriginatedHomePurchaseLoans = config.getInt("hmda.validation.macro.Q006.numOfOriginatedHomePurchaseLoans")

  override def name = "Q006"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val originatedHomePurchase =
      count(lars.filter(lar => lar.actionTakenType == 1 && lar.loan.purpose == 1))

    val homePurchase = count(lars.filter(lar => lar.loan.purpose == 1))

    for {
      o <- originatedHomePurchase
      h <- homePurchase
    } yield {
      when(o is greaterThan(numOfOriginatedHomePurchaseLoans)) {
        o.toDouble is lessThanOrEqual(h * multiplier)
      }
    }

  }
}
