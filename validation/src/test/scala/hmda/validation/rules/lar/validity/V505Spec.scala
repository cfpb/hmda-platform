package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V505Spec extends LarEditCheckSpec with BadValueUtils {
  property("When action taken not in 1-8 lar is valid") {
    forAll(larGen, intOutsideRange(1, 8)) { (lar, actionTaken) =>
      val validLar = lar.copy(actionTakenType = actionTaken)
      validLar.mustPass
    }
  }

  property("When rate spread is NA lar is valid") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(rateSpread = "NA")
      validLar.mustPass
    }
  }

  property("When ratespread in 1-8 and ratespread not NA lar is invalid") {
    forAll(larGen, Gen.choose(1, 8), Gen.numStr) { (lar, actionTaken, rate) =>
      val invalidLar: LoanApplicationRegister = lar.copy(actionTakenType = actionTaken, rateSpread = rate)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V505
}
