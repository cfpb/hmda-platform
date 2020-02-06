package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q652 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q652"

  override def parent: String = "Q652"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.loan.debtToIncomeRatio is numeric) {
        lar.loan.debtToIncomeRatio.toFloat is greaterThanOrEqual(1) or (lar.loan.debtToIncomeRatio.toFloat is lessThanOrEqual(0))
    }
  }
}
