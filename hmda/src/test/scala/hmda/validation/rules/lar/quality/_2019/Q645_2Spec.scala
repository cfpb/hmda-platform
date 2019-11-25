package hmda.validation.rules.lar.quality._2019

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.HomePurchase

class Q645_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q645_2

  property(
    "[2019] loan amount should generally be not less than 1000 when purpose is 1") {
    forAll(larGen) { lar =>
      lar
        .copy(
          loan = lar.loan.copy(
            amount = 999,
            loanPurpose = HomePurchase
          )
        )
        .mustFail
    }
  }

  property(
    "[2019] loan amount should generally be greater than 1000 when purpose is 1") {
    forAll(larGen) { lar =>
      lar
        .copy(
          loan = lar.loan.copy(
            amount = 1001,
            loanPurpose = HomePurchase
          )
        )
        .mustPass
    }
  }

}
