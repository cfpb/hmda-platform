package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V614_1 extends EditCheck[LoanApplicationRegister] {

  val purposeValues = List(HomeImprovement, Refinancing, CashOutRefinancing, OtherPurpose, LoanPurposeNotApplicable)

  override def name: String = "V614-1"

  override def parent: String = "V614"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.loanPurpose is containedIn(purposeValues)) {
      lar.action.preapproval is equalTo(PreapprovalNotRequested)
    }
}
