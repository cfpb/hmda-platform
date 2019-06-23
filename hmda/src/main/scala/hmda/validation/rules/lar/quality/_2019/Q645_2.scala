package hmda.validation.rules.lar.quality._2019

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q645_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q645-2"

  override def parent: String = "Q645"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(lar.loan.loanPurpose is equalTo(HomePurchase)) {
      lar.loan.amount is greaterThanOrEqual(1000)
    }
  }
}
