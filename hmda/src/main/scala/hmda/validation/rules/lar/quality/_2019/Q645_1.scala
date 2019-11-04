package hmda.validation.rules.lar.quality._2019

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q645_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q645-1"

  override def parent: String = "Q645"



  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val loanAmount = lar.loan.amount
    loanAmount is greaterThanOrEqual(500)
  }
}
