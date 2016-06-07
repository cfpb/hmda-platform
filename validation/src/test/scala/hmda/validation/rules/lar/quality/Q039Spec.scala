package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q039Spec extends LarEditCheckSpec with BadValueUtils {

  property("All lars with HOEPA status and action taken type not equal to 1 must pass") {
    forAll(larGen) { lar =>
      whenever(lar.hoepaStatus != 1 && lar.actionTakenType != 1) {
        lar.mustPass
      }
    }
  }

  property("Lars with HOEPA status and action taken type equal to 1 must have rate spread not equal to NA") {
    forAll(larGen) { lar =>
      whenever(lar.rateSpread != "NA") {
        val newLar = lar.copy(hoepaStatus = 1, actionTakenType = 1)
        newLar.mustPass
      }
    }
  }

  property("Lars with HOEPA status and action taken type equal to 1 and rate spread equal to NA must fail") {
    forAll(larGen) { lar =>
      val newLar = lar.copy(hoepaStatus = 1, actionTakenType = 1, rateSpread = "NA")
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q039
}
