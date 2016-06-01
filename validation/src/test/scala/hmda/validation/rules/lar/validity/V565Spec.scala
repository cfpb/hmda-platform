package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}
import org.scalacheck.Gen

class V565Spec extends LarEditCheckSpec with BadValueUtils {
  property("When action taken = 6, lien status must be 4") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(actionTakenType = 6, lienStatus = 4)
      validLar.mustPass
    }
  }

  property("When action taken != 6, lien status can be anything") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 6) {
        lar.mustPass
      }
    }
  }

  val invalidLienStatusGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 4)

  property("When action taken = 6, lien status not equal to 4 is invalid") {
    forAll(larGen, invalidLienStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(actionTakenType = 6, lienStatus = x)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V565
}
