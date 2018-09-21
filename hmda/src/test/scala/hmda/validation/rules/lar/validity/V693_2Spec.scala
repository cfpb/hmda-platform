package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ApplicationSubmissionExempt,
  PurchasedLoan,
  SubmittedDirectlyToInstitution
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V693_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V693_2

  property("If loan is purchased, application submission must be NA or exempt") {
    forAll(larGen) { lar =>
      whenever(lar.action.actionTakenType != PurchasedLoan) {
        lar.mustPass
      }

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))

      appLar
        .copy(applicationSubmission = SubmittedDirectlyToInstitution)
        .mustFail
      appLar.copy(applicationSubmission = ApplicationSubmissionExempt).mustPass
    }
  }
}
