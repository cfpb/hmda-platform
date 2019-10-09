package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V663 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V663"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is oneOf(ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan)) {
      (lar.applicant.creditScore is oneOf(1111, 8888)) and
        (lar.applicant.creditScoreType is equalTo(CreditScoreNotApplicable) or
          (lar.applicant.creditScoreType is equalTo(CreditScoreExempt))) and
        (lar.applicant.otherCreditScoreModel is empty)
    }
}
