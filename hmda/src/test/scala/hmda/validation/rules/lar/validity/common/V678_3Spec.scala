package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ReverseMortgage
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V678_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V678_3

  property(
    "If loan is a reverse mortgage, prepayment penalty term must be NA or exempt") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage != ReverseMortgage) {
        lar.mustPass
      }

      val appLar = lar.copy(reverseMortgage = ReverseMortgage)
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "-10.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "10.0"))
        .mustFail
      appLar
        .copy(loan = appLar.loan.copy(prepaymentPenaltyTerm = "NA"))
        .mustPass
    }
  }
}
