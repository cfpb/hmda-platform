package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q064Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if purchaser not 1 or 3") {
    forAll(larGen, intOtherThan(List(1, 3))) { (lar, x) =>
      val newLar = lar.copy(purchaserType = x)
      newLar.mustPass
    }
  }

  property("Valid if hoepa not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLar = lar.copy(hoepaStatus = x)
      newLar.mustPass
    }
  }

  property("Invalid if hoepa 1 and purchaser 1 or 3") {
    forAll(larGen, Gen.oneOf(1, 3)) { (lar, x) =>
      val newLar = lar.copy(hoepaStatus = 1, purchaserType = x)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q064
}
