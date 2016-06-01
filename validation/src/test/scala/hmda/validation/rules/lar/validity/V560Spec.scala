package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V560Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid if action taken = 6") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(actionTakenType = 6)
      validLar.mustPass
    }
  }

  property("Valid if lien status is 1, 2, or 3") {
    forAll(larGen) { lar =>
      whenever(lar.lienStatus != 4) {
        lar.mustPass
      }
    }
  }

  val badLienStatusGen: Gen[Int] = intOutsideRange(1, 3)

  property("Invalid when lien status not 1,2 or 3 when action is 1-5, 7 ,or 8") {
    forAll(larGen, badLienStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      whenever(lar.actionTakenType != 6) {
        val invalidLar: LoanApplicationRegister = lar.copy(lienStatus = x)
        invalidLar.mustFail
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V560
}
