package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q045Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if action not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLar = lar.copy(actionTakenType = x)
      newLar.mustPass
    }
  }

  property("Valid if lien not 2") {
    forAll(larGen, intOtherThan(2)) { (lar, x) =>
      val newLar = lar.copy(lienStatus = x)
      newLar.mustPass
    }
  }

  property("Valid when ratespread not numeric") {
    forAll(larGen, Gen.alphaStr) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x)
      newLar.mustPass
    }
  }

  property("Valid when ratespread less than or equal to 8.5") {
    forAll(larGen, Gen.choose(Double.MinValue, 8.5)) { (lar, x) =>
      val newLar = lar.copy(rateSpread = x.toString)
      newLar.mustPass
    }
  }

  property("Valid when HOEPA status = 1") {
    forAll(larGen) { (lar) =>
      val newLar = lar.copy(hoepaStatus = 1)
      newLar.mustPass
    }
  }

  property("Invalid when hoepa not 1 and other conditions met") {
    forAll(larGen, Gen.choose(8.5, Double.MaxValue), intOtherThan(1)) { (lar, r, h) =>
      val newLar = lar.copy(
        rateSpread = r.toString,
        hoepaStatus = h,
        actionTakenType = 1,
        lienStatus = 2
      )
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q045
}
