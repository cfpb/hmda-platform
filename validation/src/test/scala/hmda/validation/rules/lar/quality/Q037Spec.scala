package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q037Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid if lien staus not 2") {
    forAll(larGen, intOtherThan(2)) { (lar, x) =>
      val newLar = lar.copy(lienStatus = x)
      newLar.mustPass
    }
  }

  property("Valid if loan amount less than or equal to 250 ($250,000)") {
    forAll(larGen, Gen.choose(Int.MinValue, 250)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  property("Invalid when loan greater than 250 and lien status 2") {
    forAll(larGen, Gen.choose(251, Int.MaxValue)) { (lar, x) =>
      val newLoan = lar.loan.copy(amount = x)
      val newLar = lar.copy(loan = newLoan, lienStatus = 2)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q037
}
