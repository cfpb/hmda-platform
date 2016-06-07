package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q049Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if action not 7 or 8") {
    forAll(larGen, intOtherThan(List(7, 8))) { (lar, x) =>
      val newLar = lar.copy(actionTakenType = x)
      newLar.mustPass
    }
  }

  property("Valid when all geography = NA") {
    forAll(larGen) { (lar) =>
      val newGeo = Geography("NA", "NA", "NA", "NA")
      val newLar = lar.copy(geography = newGeo)
      newLar.mustPass
    }
  }

  property("Invalid when action 7 or 8 and not all geo fields NA") {
    forAll(larGen, Gen.oneOf(7, 8)) { (lar, x) =>
      val newGeo = lar.geography.copy(state = "45")
      val newLar = lar.copy(actionTakenType = x, geography = newGeo)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q049
}
