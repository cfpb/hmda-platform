package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V679_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V679-2"

  override def parent: String = "V679"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is oneOf(ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan)) {
      lar.loan.debtToIncomeRatio is oneOf("NA", "Exempt")
    }
}
