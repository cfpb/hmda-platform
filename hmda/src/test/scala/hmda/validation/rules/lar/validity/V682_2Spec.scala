package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ReverseMortgage
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V682_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V682_2

  property("Loan term must be valid") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage != ReverseMortgage) {
        lar.mustPass
      }

      val invalidLar = lar.copy(loan = lar.loan.copy(loanTerm = "1"),
                                reverseMortgage = ReverseMortgage)
      invalidLar.mustFail

      val validLar = lar.copy(loan = lar.loan.copy(loanTerm = "NA"),
                              reverseMortgage = ReverseMortgage)
      validLar.mustPass
    }
  }
}
