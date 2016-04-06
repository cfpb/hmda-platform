package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V340Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with PurchaserTypeUtils {

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
