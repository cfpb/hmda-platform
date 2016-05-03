package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V255Spec extends LarEditCheckSpec with BadValueUtils {
  property("Succeeds when Action Taken Type = 1, 2, 3, 4, 5, 6, 7, or 8") {
    forAll(larGen) { lar =>
      V255(lar) mustBe Success()
    }
  }

  property("Fails when Action Taken Type is not valid") {
    forAll(larGen, intOutsideRange(1, 8)) { (lar, action) =>
      val invalidLAR = lar.copy(actionTakenType = action)
      V255(invalidLAR) mustBe a[Failure]
    }
  }

}
