package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V614_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V614_2

  property("Non-numeric multi-family affordable units must pass") {
    forAll(larGen) { lar =>
      whenever(lar.property.multiFamilyAffordableUnits == "NA") {
        lar.mustPass
      }
    }
  }

  property(
    "Numeric multi-family affordable units must have no preapproval requested") {
    forAll(larGen) { lar =>
      whenever(lar.property.multiFamilyAffordableUnits != "NA") {
        lar
          .copy(action = lar.action.copy(preapproval = PreapprovalNotRequested))
          .mustPass
        lar
          .copy(action = lar.action.copy(preapproval = PreapprovalRequested))
          .mustFail
      }
    }
  }
}
