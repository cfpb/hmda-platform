package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V505Spec extends LarEditCheckSpec with BadValueUtils {
  property("When action taken not in 2-8 lar is valid") {
    forAll(larGen, intOutsideRange(2, 8)) { (lar, actionTaken) =>
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

  property("When action taken in 2-8 and ratespread not NA lar is invalid") {
    forAll(larGen, Gen.choose(2, 8)) { (lar, actionTaken) =>
      whenever(lar.rateSpread != "NA") {
        val invalidLar: LoanApplicationRegister = lar.copy(actionTakenType = actionTaken)
        invalidLar.mustFail
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V505
}
