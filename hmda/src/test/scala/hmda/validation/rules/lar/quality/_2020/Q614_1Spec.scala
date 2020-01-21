package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q614_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q614_1

  property(
    "Income should be less than $10 million") {
    forAll(larGen) { lar =>
      lar.copy(applicant = lar.applicant.copy(age = 8888)).mustPass
      lar.copy(applicant = lar.applicant.copy(age = 17)).mustFail
      lar.copy(applicant = lar.applicant.copy(age = 18)).mustPass
      lar.copy(applicant = lar.applicant.copy(age = 100)).mustPass
      lar.copy(applicant = lar.applicant.copy(age = 101)).mustFail
    }
  }
}
