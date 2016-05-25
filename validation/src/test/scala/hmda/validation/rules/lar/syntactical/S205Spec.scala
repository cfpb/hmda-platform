package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class S205Spec extends LarEditCheckSpec {
  property("All lars must have a valid loan ID") {
    forAll(larGen) { lar =>
      whenever(!lar.loan.id.forall(_ == '0') && lar.loan.id.length != 0) {
        lar.mustPass
      }
    }
  }

  property("Lar with an empty id must fail") {
    forAll(larGen) { lar =>
      val invalidLoan = lar.loan.copy(id = "")
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  property("Lar with an ID with all 0's must fail") {
    forAll(larGen) { lar =>
      val invalidLoan = lar.loan.copy(id = "0000000000000")
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = S205
}
