package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q614_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q614_2

  property(
    "Age should be between 18 and 100") {
    forAll(larGen) { lar =>
      lar.copy(coApplicant = lar.coApplicant.copy(age = 8888)).mustPass
      lar.copy(coApplicant = lar.coApplicant.copy(age = 9999)).mustPass
      lar.copy(coApplicant = lar.coApplicant.copy(age = 17)).mustFail
      lar.copy(coApplicant = lar.coApplicant.copy(age = 18)).mustPass
      lar.copy(coApplicant = lar.coApplicant.copy(age = 100)).mustPass
      lar.copy(coApplicant = lar.coApplicant.copy(age = 101)).mustFail
    }
  }
}
