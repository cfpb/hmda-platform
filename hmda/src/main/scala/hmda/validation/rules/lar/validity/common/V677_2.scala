package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V677_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V677-2"

  override def parent: String = "V677"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.action.actionTakenType is
        oneOf(ApplicationDenied, ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PreapprovalRequestDenied)
    ) {
      lar.loan.interestRate is oneOf("NA", "Exempt")
    }
}
