package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q649_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q649_1

  property(
    "Applicant credit score should be between 300 and 900 with exceptions") {
    
    forAll(larGen) { lar =>
        lar.copy(applicant = lar.applicant.copy(creditScore = 7777)).mustPass
        lar.copy(applicant = lar.applicant.copy(creditScore = 8888)).mustPass
        lar.copy(applicant = lar.applicant.copy(creditScore = 1111)).mustPass
        lar.copy(applicant = lar.applicant.copy(creditScore = 299)).mustFail
        lar.copy(applicant = lar.applicant.copy(creditScore = 300)).mustPass
        lar.copy(applicant = lar.applicant.copy(creditScore = 900)).mustPass
        lar.copy(applicant = lar.applicant.copy(creditScore = 901)).mustFail

    }
  }
}
