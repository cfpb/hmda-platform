package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q001 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(
      (lar.loan.amount is numeric) and
        (lar.applicant.income is numeric)
    ) {
        when(
          (lar.applicant.income.toInt is greaterThan(0)) and
            (lar.loan.amount is greaterThanOrEqual(1000))
        ) {
            lar.loan.amount is lessThan(lar.applicant.income.toInt * 5)
          }
      }
  }

  override def name = "Q001"
}
