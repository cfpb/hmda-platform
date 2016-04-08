package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec

class V340Spec extends LarEditCheckSpec with PurchaserTypeUtils {

  property("Succeeds when Type of Purchaser = 0, 1, 2, 3, 4, 5, 6, 7, 8, or 9.") {
    forAll(larGen) { lar =>
      V340(lar) mustBe Success()
    }
  }

  property("Fails when purchaser type is not valid") {
    forAll(larGen, badPurchaserTypeGen) { (lar, pt) =>
      val invalidLAR = lar.copy(purchaserType = pt)
      V340(invalidLAR) mustBe Failure("is not contained in valid values domain")
    }
  }

}
