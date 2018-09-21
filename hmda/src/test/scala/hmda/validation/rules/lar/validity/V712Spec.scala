package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V712Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V712

  property("When loan costs or points and fees is exempt, both must be exempt") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(
        loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "test",
                                                 totalPointsAndFees = "test"))
      unappLar.mustPass

      val invalidLar1 = lar.copy(
        loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "Exempt",
                                                 totalPointsAndFees = "test"))
      invalidLar1.mustFail
      val invalidLar2 = lar.copy(
        loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "test",
                                                 totalPointsAndFees = "Exempt"))
      invalidLar2.mustFail

      val validLar = lar.copy(
        loanDisclosure = lar.loanDisclosure.copy(totalLoanCosts = "Exempt",
                                                 totalPointsAndFees = "Exempt"))
      validLar.mustPass
    }
  }
}
