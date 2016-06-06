package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q025 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q025.loan.amount")

    when((lar.loan.purpose is equalTo(1)) and (lar.loan.propertyType is equalTo(1))) {
      lar.loan.amount is greaterThan(loanAmount)
    }
  }

  override def name = "Q025"
}
