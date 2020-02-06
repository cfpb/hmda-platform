package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q651 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q651"

  override def parent: String = "Q651"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.loan.combinedLoanToValueRatio is numeric) {
        lar.loan.combinedLoanToValueRatio.toFloat is greaterThanOrEqual(1) or (lar.loan.combinedLoanToValueRatio.toFloat is lessThanOrEqual(0))
    }
  }
}
