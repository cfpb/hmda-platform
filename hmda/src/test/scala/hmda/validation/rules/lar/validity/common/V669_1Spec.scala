package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V669_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V669_1

  property("Denial reason 1 must be valid") {
    forAll(larGen) { lar =>
      whenever(lar.denial.denialReason1 != EmptyDenialValue) {
        lar.mustPass
      }

      lar
        .copy(denial = lar.denial.copy(denialReason1 = EmptyDenialValue))
        .mustFail
    }
  }
}
