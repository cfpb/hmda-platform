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

object Q056 extends AggregateEditCheck[LoanApplicationRegisterSource, LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val homeLoanCount = config.getInt("hmda.validation.macro.Q056.numOfConventionalHomePurchaseLoans")
  val homeLoanDeniedMultiplier = config.getDouble("hmda.validation.macro.Q056.deniedConventionalHomePurchaseLoansMultiplier")

  override def name = "Q056"

  override def apply[as: AS, mat: MAT, ec: EC](lars: LoanApplicationRegisterSource): Future[Result] = {

    val denied =
      count(lars.filter(lar => lar.loan.purpose == 1 && lar.loan.loanType == 1 && lar.actionTakenType == 3))

    val total =
      count(lars.filter(lar => lar.loan.purpose == 1 && lar.loan.loanType == 1))

    for {
      d <- denied
      t <- total
    } yield {
      when(t is greaterThanOrEqual(homeLoanCount)) {
        d.toDouble is lessThanOrEqual(t * homeLoanDeniedMultiplier)
      }
    }

  }
}
