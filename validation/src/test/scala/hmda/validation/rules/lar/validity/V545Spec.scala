package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}
import org.scalacheck.Gen

class V545Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid when HOEPA status is 2") {
    forAll(larGen) { lar =>
      whenever(lar.hoepaStatus == 2) {
        lar.mustPass
      }
    }
  }

  property("Valid when Lien status != 3") {
    forAll(larGen) { lar =>
      whenever(lar.lienStatus != 3) {
        lar.mustPass
      }
    }
  }

  val invalidHoepaGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 2)

  property("Invalid when lien status is 3 and HOEPA status not 2") {
    forAll(larGen, invalidHoepaGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar = lar.copy(lienStatus = 3, hoepaStatus = x)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V545
}
