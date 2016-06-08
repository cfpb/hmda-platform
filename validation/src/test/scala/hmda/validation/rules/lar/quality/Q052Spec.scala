package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }

class Q052Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if property type not 3") {
    forAll(larGen, intOtherThan(3)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
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

  property("Invalid if hopea 1 and property type 3") {
    forAll(larGen) { (lar) =>
      val newLoan = lar.loan.copy(propertyType = 3)
      val newLar = lar.copy(hoepaStatus = 1, loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q052
}
