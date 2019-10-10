package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q615_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q615_1

  property("Total loan costs should be greater than origination charges") {
    forAll(larGen) { lar =>
      whenever(
        lar.loanDisclosure.totalLoanCosts == "NA" || lar.loanDisclosure.originationCharges == "NA") {
        lar.mustPass
      }

      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "2.0",
                                                   originationCharges = "1.0"))
        .mustPass
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "1.0",
                                                   originationCharges = "1.0"))
        .mustFail
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "0.0",
                                                   originationCharges = "1.0"))
        .mustPass
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "0.0",
                                                   originationCharges = "0.0"))
        .mustPass
    }
  }
}
