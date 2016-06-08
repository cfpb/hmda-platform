package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q002 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {

    val config = ConfigFactory.load()
    val loanAmount = config.getInt("hmda.validation.quality.Q002.loan.amount")
    val income = config.getInt("hmda.validation.quality.Q002.applicant.income")

    when((lar.loan.propertyType is equalTo(1)) and (lar.applicant.income is numeric)) {
      when(lar.applicant.income.toInt is lessThanOrEqual(income)) {
        lar.loan.amount is lessThan(loanAmount)
      }
    }
  }

  override def name = "Q002"
}
