package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q067Spec extends LarEditCheckSpec {
  // Cases meeting preconditions
  property("fails when income not NA and applicant and coapplicant are both not a natural person") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.income != "NA" && lar.actionTakenType != 6) {
        val notNaturalApplicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4,
          coEthnicity = 4, coRace1 = 7, coSex = 4)
        val invalidLar = lar.copy(applicant = notNaturalApplicant)
        invalidLar.mustFail
      }
    }
  }

  property("passes when applicant and coapplicant are not a natural person, but income = NA") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 6) {
        val notNaturalApplicant = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4,
          coEthnicity = 4, coRace1 = 7, coSex = 4,
          income = "NA")
        val validLar = lar.copy(applicant = notNaturalApplicant)
        validLar.mustPass
      }
    }
  }

  // Cases not meeting preconditions
  property("passes when applicant is a natural person (CoApplicant not natural person)") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.ethnicity != 4 && lar.actionTakenType != 6) {
        val notNaturalCoApp = lar.applicant.copy(coEthnicity = 4, coRace1 = 7, coSex = 4)
        val validLar = lar.copy(applicant = notNaturalCoApp)
        validLar.mustPass
      }
    }
  }

  property("passes when coapplicant is a natural person (Applicant not natural person") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.coEthnicity != 4 && lar.actionTakenType != 6) {
        val notNaturalApp = lar.applicant.copy(ethnicity = 4, race1 = 7, sex = 4)
        val validLar = lar.copy(applicant = notNaturalApp)
        validLar.mustPass
      }
    }
  }

  property("passes when action taken type is not 1-5,7,8") {
    forAll(larGen) { lar =>
      val validLar = lar.copy(actionTakenType = 6)
      validLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q067
}
