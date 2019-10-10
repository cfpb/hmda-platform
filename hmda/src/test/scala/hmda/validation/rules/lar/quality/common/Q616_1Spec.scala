package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q616_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q616_1

  property("Total loan costs should be greater than discount points") {
    forAll(larGen) { lar =>
      whenever(
        lar.loanDisclosure.totalLoanCosts == "NA" || lar.loanDisclosure.discountPoints == "NA") {
        lar.mustPass
      }

      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "2.0",
                                                   discountPoints = "1.0"))
        .mustPass
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "1.0",
                                                   discountPoints = "1.0"))
        .mustFail
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "0.0",
                                                   discountPoints = "1.0"))
        .mustPass
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "0.0",
                                                   discountPoints = "0.0"))
        .mustPass
    }
  }
}
