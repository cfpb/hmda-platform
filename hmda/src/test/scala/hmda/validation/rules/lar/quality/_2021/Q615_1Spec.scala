package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q615_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q615_1

  property(
    "Total points and fees should be greater than origination charges") {
    forAll(larGen) { lar =>
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "NA", originationCharges = "999")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "999", originationCharges = "NA")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "NA", originationCharges = "NA")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "999.1", originationCharges = "999")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "999", originationCharges = "999.1")).mustFail
    }
  }
}
