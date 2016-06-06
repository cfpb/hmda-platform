package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q013Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if property type not 3") {
    forAll(larGen, intOtherThan(3)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Valid if loan amount between 100 ($100,000) and 10000 ($10,000,000)") {
    forAll(larGen, Gen.choose(100, 10000)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Invalid when loan not between 100 ($100,000) and 10000 ($10,000,000) and propety type 3") {
    forAll(larGen, intOutsideRange(100, 10000)) { (lar, x) =>
      val newLoan = lar.loan.copy(propertyType = 3, amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q013
}
