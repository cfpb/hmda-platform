package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V535Spec extends LarEditCheckSpec with BadValueUtils {
  property("Valid if coethnicity not 4") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.ethnicity != 4) {
        lar.mustPass
      }
    }
  }

  property("Valid if corace not 7") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.race1 != 7) {
        lar.mustPass
      }
    }
  }

  property("Valid if cosex not 4") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.sex != 4) {
        lar.mustPass
      }
    }
  }

  property("Valid if action taken is 6") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(actionTakenType = 6)
      validLar.mustPass
    }
  }

  property("Valid if heopa not 1") {
    forAll(larGen) { lar =>
      whenever(lar.hoepaStatus != 1) {
        lar.mustPass
      }
    }
  }

  val badActionTakenGen: Gen[Int] = Gen.choose(Int.MinValue, Int.MaxValue).filter(_ != 6)

  property("HOEPA status other than 1 is invalid when other conditions met") {
    forAll(larGen, badActionTakenGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidApplicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4)
      val invalidLar: LoanApplicationRegister = lar.copy(
        actionTakenType = x,
        applicant = invalidApplicant,
        hoepaStatus = 1
      )
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V535
}
