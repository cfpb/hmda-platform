package hmda.validation.rules.lar.validity

import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V255Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("Succeeds when Action Taken Type = 1, 2, 3, 4, 5, 6, 7, or 8") {
    forAll(larGen) { lar =>
      V255(lar) mustBe Success()
    }
  }

  property("Fails when Action Taken Type is not valid") {
    forAll(larGen, Gen.oneOf(Gen.negNum[Int], Gen.choose(9, Integer.MAX_VALUE))) { (lar, action) =>
      val invalidLAR = lar.copy(actionTakenType = action)
      V255(invalidLAR) mustBe a[Failure]
    }
  }

}
