package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums.InvalidLoanTypeCode
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V611Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V611

  property("Loan Type must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val badLoan = lar.loan.copy(loanType = new InvalidLoanTypeCode)
      lar.copy(loan = badLoan).mustFail
    }
  }
}
