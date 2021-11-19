package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V721_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V721_1

  property("Borrower’s age and cannot be 9999 or 1111") {
    forAll(larGen) { lar =>
      val badAge = lar.applicant.copy(age = 9999)
      val altBadAge = lar.applicant.copy(age = 1111)
      val goodAge = lar.applicant.copy(age = 37)
      val altGoodAge = lar.applicant.copy(age = 99)

      lar.copy(applicant = badAge).mustFail
      lar.copy(applicant = altBadAge).mustFail

      lar.copy(applicant = goodAge).mustPass
      lar.copy(applicant = altGoodAge).mustPass
    }
  }
}