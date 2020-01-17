package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  InititallyPayableToInstitution,
  InvalidPayableToInstitutionCode,
  LoanOriginated
}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V694_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V694_3

  property("If loan is originated, initially payable must be valid") {
    forAll(larGen) { lar =>
      whenever(lar.action.actionTakenType != LoanOriginated) {
        lar.mustPass
      }

      val appLar =
        lar.copy(action = lar.action.copy(actionTakenType = LoanOriginated))

      val invalidLar =
        appLar.copy(payableToInstitution = new InvalidPayableToInstitutionCode)
      invalidLar.mustFail

      val validLar =
        appLar.copy(payableToInstitution = InititallyPayableToInstitution)
      validLar.mustPass
    }
  }
}
