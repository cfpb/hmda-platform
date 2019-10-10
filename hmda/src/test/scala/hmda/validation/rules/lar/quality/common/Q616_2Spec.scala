package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q616_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q616_2

  property("Total points and fees should be greater than origination charges") {
    forAll(larGen) { lar =>
      whenever(
        lar.loanDisclosure.totalPointsAndFees == "NA" || lar.loanDisclosure.discountPoints == "NA") {
        lar.mustPass
      }

      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "2.0",
                                                   discountPoints = "1.0"))
        .mustPass
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "1.0",
                                                   discountPoints = "1.0"))
        .mustFail
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "0.0",
                                                   discountPoints = "1.0"))
        .mustPass
      lar
        .copy(
          loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "0.0",
                                                   discountPoints = "0.0"))
        .mustPass
    }
  }
}
