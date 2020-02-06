package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q649_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q649_2

  property(
    "CoApplicant credit score should be between 300 and 900 with exceptions") {
    
    forAll(larGen) { lar =>
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 7777)).mustPass
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 8888)).mustPass
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 1111)).mustPass
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 299)).mustFail
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 300)).mustPass
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 900)).mustPass
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 901)).mustFail

    }
  }
}
