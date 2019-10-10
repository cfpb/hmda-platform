package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q614Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q614

  property("Age must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar.copy(applicant = lar.applicant.copy(age = 8888)).mustPass
      lar.copy(applicant = lar.applicant.copy(age = 101)).mustFail
      lar.copy(applicant = lar.applicant.copy(age = 17)).mustFail
    }
  }
}
