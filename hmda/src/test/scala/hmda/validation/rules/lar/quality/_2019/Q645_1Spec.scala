package hmda.validation.rules.lar.quality._2019

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.HomeImprovement

class Q645_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q645_1

  property("[2019] loan amount should generally be not less than 500") {
    forAll(larGen) { lar =>
      lar
        .copy(
          loan = lar.loan.copy(loanPurpose = HomeImprovement, amount = 499)
        )
        .mustFail
    }
  }

  property("[2019] loan amount should generally be greater than 500") {
    forAll(larGen) { lar =>
      lar
        .copy(
          loan = lar.loan.copy(loanPurpose = HomeImprovement, amount = 501)
        )
        .mustPass
    }
  }

}
