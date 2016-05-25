package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.parser.fi.FIGenerators
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V215Spec extends LarEditCheckSpec with FIGenerators {
  property("Passes if loan application date is NA") {
    forAll(larGen) { lar =>
      val naLoan = lar.loan.copy(applicationDate = "NA")
      val newLar = lar.copy(loan = naLoan)
      newLar.mustPass
    }
  }

  property("Passes if actionTakenType is not 6") {
    forAll(larGen) { lar =>
      whenever(lar.actionTakenType != 6) {
        lar.mustPass
      }
    }
  }

  property("Fails if actionTakenType is 6 and loan application date is not NA") {
    forAll(larGen, dateGen) { (lar, date) =>
      val naLoan = lar.loan.copy(applicationDate = date.toString)
      val newLar = lar.copy(actionTakenType = 6, loan = naLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V215
}
