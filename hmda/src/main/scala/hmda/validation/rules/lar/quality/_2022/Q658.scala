package hmda.validation.rules.lar.quality._2022

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q658 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q658"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val loanTerm = lar.loan.loanTerm
    when(loanTerm is numeric) {
      loanTerm.toInt is lessThanOrEqual(600)
    }
  }
}
