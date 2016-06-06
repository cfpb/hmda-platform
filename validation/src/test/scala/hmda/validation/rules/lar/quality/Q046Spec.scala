package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }

class Q046Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if loan purpose not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLoan = lar.loan.copy(purpose = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Valid if hoepa not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, x) =>
      val newLar = lar.copy(hoepaStatus = x)
      newLar.mustPass
    }
  }

  property("Invalid if hopea 1 and loan purpose 1") {
    forAll(larGen) { (lar) =>
      val newLoan = lar.loan.copy(purpose = 1)
      val newLar = lar.copy(hoepaStatus = 1, loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q046
}
