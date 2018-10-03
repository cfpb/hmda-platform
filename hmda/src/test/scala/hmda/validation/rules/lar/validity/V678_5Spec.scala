package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V678_5Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V678_5

  property("Penalty term must be less than or equal to loan term") {
    forAll(larGen) { lar =>
      val unappLar1 = lar.copy(loan = lar.loan.copy(loanTerm = "test"))
      val unappLar2 =
        lar.copy(loan = lar.loan.copy(prepaymentPenaltyTerm = "test"))
      unappLar1.mustPass
      unappLar2.mustPass

      val badLar = lar.copy(
        loan = lar.loan.copy(loanTerm = "60", prepaymentPenaltyTerm = "61"))
      badLar.mustFail

      val equalLar = lar.copy(
        loan = lar.loan.copy(loanTerm = "60", prepaymentPenaltyTerm = "60"))
      equalLar.mustPass

      val goodLar = lar.copy(
        loan = lar.loan.copy(loanTerm = "60", prepaymentPenaltyTerm = "59"))
      goodLar.mustPass
    }
  }
}
