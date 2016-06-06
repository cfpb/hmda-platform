package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.FIGenerators
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q032Spec extends LarEditCheckSpec with FIGenerators {

  property("All lars with action taken types not equal to 1 must pass") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 1) {
        lar.mustPass
      }
    }
  }

  property("Lars with action taken = 1 must have different application and action taken dates") {
    forAll(larGen, dateGen) { (lar, x) =>
      val newLoan = lar.loan.copy(applicationDate = x.toString)
      val newLar = lar.copy(actionTakenType = 1, actionTakenDate = x + 1, loan = newLoan)
      newLar.mustPass
    }
  }

  property("Lars with action taken = 1 and equal application and action taken dates must fail") {
    forAll(larGen, dateGen) { (lar, x) =>
      val newLoan = lar.loan.copy(applicationDate = x.toString)
      val newLar = lar.copy(actionTakenType = 1, actionTakenDate = x, loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q032
}
