package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V255Spec extends LarEditCheckSpec with BadValueUtils {
  property("Succeeds when Action Taken Type = 1, 2, 3, 4, 5, 6, 7, or 8") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  property("Fails when Action Taken Type is not valid") {
    forAll(larGen, intOutsideRange(1, 8)) { (lar, action) =>
      val invalidLAR = lar.copy(actionTakenType = action)
      invalidLAR.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V255
}
