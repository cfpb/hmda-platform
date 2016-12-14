package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import com.typesafe.config.ConfigFactory

object Q038 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q038.loan.amount")

    when(lar.lienStatus is equalTo(3)) {
      lar.loan.amount is lessThanOrEqual(loanAmount)
    }
  }

  override def name = "Q038"
}
