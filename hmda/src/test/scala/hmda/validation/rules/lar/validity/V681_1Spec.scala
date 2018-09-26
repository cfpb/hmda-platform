package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V681_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V681_1

  property("Combined loan to value ratio must be valid") {
    forAll(larGen) { lar =>
      val invalidLar1 =
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "test"))
      invalidLar1.mustFail

      val invalidLar2 =
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "-5"))
      invalidLar2.mustFail

      val naLar =
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "NA"))
      naLar.mustPass

      val validLar1 =
        lar.copy(loan = lar.loan.copy(combinedLoanToValueRatio = "1.0"))
      validLar1.mustPass
    }
  }
}
