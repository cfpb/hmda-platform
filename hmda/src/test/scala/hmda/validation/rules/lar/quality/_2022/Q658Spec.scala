package hmda.validation.rules.lar.quality._2022

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q658Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q658

  property("If Loan Term is not NA or Exempt, the Loan Term should generally be 600 or less.\n") {
    forAll(larGen) { lar =>
      lar.copy(loan = lar.loan.copy(loanTerm = "NA")).mustPass
      lar.copy(loan = lar.loan.copy(loanTerm = "Exempt")).mustPass
      lar.copy(loan = lar.loan.copy(loanTerm = "601")).mustFail
      lar.copy(loan = lar.loan.copy(loanTerm = "40")).mustPass
      lar.copy(loan = lar.loan.copy(loanTerm = "600")).mustPass
      lar.copy(loan = lar.loan.copy(loanTerm = "1337")).mustFail
    }
  }
}
