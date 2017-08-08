package hmda.validation.rules.lar.quality

import hmda.model.fi.FIGenerators
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q032Spec extends LarEditCheckSpec with FIGenerators {

  property("All lars with action taken types not equal to 1 must pass") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 1) {
        lar.mustPass
      }
    }
  }

  property("Lars with action taken = 1 must have different application and action taken dates") {
    forAll(larGen) { lar =>
      val newLoan = lar.loan.copy(applicationDate = (lar.actionTakenDate - 1).toString)
      val newLar = lar.copy(actionTakenType = 1, loan = newLoan)
      newLar.mustPass
    }
  }

  property("Lars with action taken = 1 and equal application and action taken dates must fail") {
    forAll(larGen) { lar =>
      val newLoan = lar.loan.copy(applicationDate = lar.actionTakenDate.toString)
      val newLar = lar.copy(actionTakenType = 1, loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q032
}
