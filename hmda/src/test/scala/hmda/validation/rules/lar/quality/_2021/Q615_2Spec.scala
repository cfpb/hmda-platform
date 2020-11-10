package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q615_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q615_2

  property(
    "Loan costs should be greater than origination charges") {
    forAll(larGen) { lar =>
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "NA", originationCharges = "999")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "999", originationCharges = "NA")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "NA", originationCharges = "NA")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "999.1", originationCharges = "999")).mustPass
      lar.copy(loanDisclosure = lar.loanDisclosure.copy(totalPointsAndFees = "999", originationCharges = "999.1")).mustFail
    }
  }
}
