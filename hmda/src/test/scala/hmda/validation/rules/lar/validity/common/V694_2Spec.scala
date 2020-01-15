package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  InvalidPayableToInstitutionCode,
  PayableToInstitutionNotApplicable,
  PurchasedLoan
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V694_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V694_2

  property("If loan is purchased, initially payable must be NA or exempt") {
    forAll(larGen) { lar =>
      whenever(lar.action.actionTakenType != PurchasedLoan) {
        lar.mustPass
      }

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = PurchasedLoan))

      val invalidLar =
        appLar.copy(payableToInstitution = new InvalidPayableToInstitutionCode)
      invalidLar.mustFail

      val validLar =
        appLar.copy(payableToInstitution = PayableToInstitutionNotApplicable)
      validLar.mustPass
    }
  }
}
