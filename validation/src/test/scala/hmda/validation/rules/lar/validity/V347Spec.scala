package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.{ MustMatchers, PropSpec }
import org.scalatest.prop.PropertyChecks

class V347Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators with PurchaserTypeUtils {

  property("Succeeds when there is a relevant purchaser type (1-9) and loan was originated or purchased (1 or 6)") {
    forAll(larGen, Gen.choose(1, 9), Gen.oneOf(1, 6)) { (lar, pt, action) =>
      val newLar = lar.copy(purchaserType = pt, actionTakenType = action)
      V347(newLar) mustBe Success()
    }
  }

  property("Fails when there is a relevant purchaser type (1-9) but loan was not originated (1) nor purchased (6)") {
    forAll(larGen, Gen.choose(1, 9)) { (lar, pt) =>
      whenever(!List(1, 6).contains(lar.actionTakenType)) {
        val newLar = lar.copy(purchaserType = pt)
        V347(newLar) mustBe a[Failure]
      }
    }
  }

  property("Succeeds when Type of Purchaser = 0") {
    forAll(larGen) { lar =>
      V347(lar.copy(purchaserType = 0)) mustBe Success()
    }
  }

  property("Succeeds when Type of Purchaser is invalid") {
    forAll(badPurchaserTypeLarGen) { lar =>
      V347(lar) mustBe Success()
    }
  }
}
