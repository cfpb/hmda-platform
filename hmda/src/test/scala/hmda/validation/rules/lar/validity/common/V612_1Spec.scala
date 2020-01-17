package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidLoanPurposeCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V612_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V612_1

  property("Loan Purpose must be valid") {
    forAll(larGen) { l =>
      l.mustPass
      val badLoan = l.loan.copy(loanPurpose = new InvalidLoanPurposeCode)
      l.copy(loan = badLoan).mustFail
    }
  }

}
