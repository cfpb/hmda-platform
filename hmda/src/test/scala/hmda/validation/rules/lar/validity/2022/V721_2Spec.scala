package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V721_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V721_2

  property("Borrower’s age and cannot be 1111") {
    forAll(larGen) { lar =>
      val goodAge = lar.coApplicant.copy(age = 9999)
      val altBadAge = lar.coApplicant.copy(age = 1111)
      val altGoodAge = lar.coApplicant.copy(age = 99)

      lar.copy(coApplicant = goodAge).mustPass
      lar.copy(coApplicant = altBadAge).mustFail
      lar.copy(coApplicant = altGoodAge).mustPass
    }
  }
}