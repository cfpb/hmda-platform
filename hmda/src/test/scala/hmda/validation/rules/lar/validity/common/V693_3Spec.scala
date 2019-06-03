package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  ApplicationSubmissionNotApplicable,
  PreapprovalRequestDenied,
  PurchasedLoan
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V693_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V693_3

  property("If application submission is NA, loan must be purchased") {
    forAll(larGen) { lar =>
      whenever(lar.applicationSubmission != ApplicationSubmissionNotApplicable) {
        lar.mustPass
      }

      val appLar =
        lar.copy(applicationSubmission = ApplicationSubmissionNotApplicable)

      appLar
        .copy(
          action = lar.action.copy(actionTakenType = PreapprovalRequestDenied))
        .mustFail
      appLar
        .copy(action = lar.action.copy(actionTakenType = PurchasedLoan))
        .mustPass
    }
  }
}
