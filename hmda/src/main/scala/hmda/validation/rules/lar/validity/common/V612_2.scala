package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V612_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V612-2"

  override def parent: String = "V612"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.preapproval is equalTo(PreapprovalRequested)) {
      lar.loan.loanPurpose is equalTo(HomePurchase)
    }

}
