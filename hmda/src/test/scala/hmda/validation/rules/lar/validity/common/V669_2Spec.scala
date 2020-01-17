package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V669_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V669_2

  property("Denial reason 2-4 must be valid") {
    forAll(larGen) { lar =>
      lar
        .copy(denial = lar.denial.copy(denialReason2 = new InvalidDenialReasonCode))
        .mustFail
      lar
        .copy(denial = lar.denial.copy(denialReason3 = new InvalidDenialReasonCode))
        .mustFail
      lar
        .copy(denial = lar.denial.copy(denialReason4 = new InvalidDenialReasonCode))
        .mustFail

      lar
        .copy(
          denial = lar.denial.copy(denialReason2 = EmptyDenialValue,
                                   denialReason3 = EmptyDenialValue,
                                   denialReason4 = EmptyDenialValue))
        .mustPass
    }
  }
}
