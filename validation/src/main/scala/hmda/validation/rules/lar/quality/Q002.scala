package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q002 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.loan.propertyType is equalTo(1)) and (lar.applicant.income is numeric)) {
      when(lar.applicant.income.toInt is lessThanOrEqual(200)) {
        lar.loan.amount is lessThan(2000)
      }
    }
  }

  override def name = "Q002"
}
