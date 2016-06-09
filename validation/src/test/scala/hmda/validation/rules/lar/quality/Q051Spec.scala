package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }

class Q051Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if applicant is a natural person") {
    forAll(larGen) { lar =>
      whenever((lar.applicant.ethnicity != 4) &&
        (lar.applicant.race1 != 7) &&
        (lar.applicant.sex != 4)) {
        lar.mustPass
      }
    }
  }

  property("Valid if action taken is not 6") {
    forAll(larGen, intOtherThan(6)) { (lar, x) =>
      val newLar = lar.copy(actionTakenType = x)
      newLar.mustPass
    }
  }

  property("Valid if not a natural person, action taken is 6, and HOEPA status is not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newApplicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4)
      val newLar = lar.copy(actionTakenType = 6, hoepaStatus = x, applicant = newApplicant)
      newLar.mustPass
    }
  }

  property("Invalid if not a natural person, action taken is 6, and HOEPA status is equal to 1") {
    forAll(larGen) { lar =>
      val newApplicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4)
      val newLar = lar.copy(actionTakenType = 6, hoepaStatus = 1, applicant = newApplicant)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q051
}
