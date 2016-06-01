package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}

class V340Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds when Type of Purchaser = 0, 1, 2, 3, 4, 5, 6, 7, 8, or 9.") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  property("Fails when purchaser type is not valid") {
    forAll(larGen, badPurchaserTypeGen) { (lar, pt) =>
      val invalidLAR = lar.copy(purchaserType = pt)
      invalidLAR.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V340
}
