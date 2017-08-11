package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V317Spec extends LarEditCheckSpec {
  property("Succeeds when Co-Applicant sex != 5 and ethnicity !=5") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.coSex != 5 && lar.applicant.coEthnicity != 5) {
        lar.mustPass
      }
    }
  }

  property("Succeeds when Co-Applicant race1=8, sex=5, and ethnicity=5") {
    forAll(larGen) { lar =>
      val applicantWithoutCoApp = lar.applicant.copy(coSex = 5, coRace1 = 8, coEthnicity = 5)
      val larWithoutCoApp = lar.copy(applicant = applicantWithoutCoApp)
      larWithoutCoApp.mustPass
    }
  }

  property("Fails when Co-Applicant sex=5, and race1!=8") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.coRace1 != 8) {
        val invalidApplicant = lar.applicant.copy(coSex = 5)
        val invalidLar = lar.copy(applicant = invalidApplicant)
        invalidLar.mustFail
      }
    }
  }

  property("Fails whenever Applicant ethnicity=5, and race1!=8") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.coRace1 != 8) {
        val invalidApplicant = lar.applicant.copy(coEthnicity = 5)
        val invalidLar = lar.copy(applicant = invalidApplicant)
        invalidLar.mustFail
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V317
}

