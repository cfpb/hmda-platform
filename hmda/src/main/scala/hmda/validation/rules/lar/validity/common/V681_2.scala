package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V681_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V681-2"

  override def parent: String = "V681"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is oneOf(ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan)) {
      lar.loan.combinedLoanToValueRatio is oneOf("NA", "Exempt")
    }
}
