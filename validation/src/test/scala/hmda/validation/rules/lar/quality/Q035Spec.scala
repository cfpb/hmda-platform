package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q035Spec extends LarEditCheckSpec with BadValueUtils {

  property("All lars with purchaser types not equal to 1 or 3 must pass") {
    forAll(larGen) { lar =>
      whenever(lar.purchaserType != 1 && lar.purchaserType != 3) {
        lar.mustPass
      }
    }
  }

  val purchaserGen: Gen[Int] = Gen.oneOf(1, 3)

  property("Lars with purchaser type equal to 1 or 3 must have loan type equal to 1") {
    forAll(larGen, purchaserGen) { (lar, x) =>
      val newLoan = lar.loan.copy(loanType = 1)
      val newLar = lar.copy(loan = newLoan, purchaserType = x)
      newLar.mustPass
    }
  }

  val validLoanType: Gen[Int] = intOtherThan(1)

  property("Lars with purchaser type equal to 1 or 3 and a loan type not equal to 1 must fail") {
    forAll(larGen, validLoanType, purchaserGen) { (lar, x, y) =>
      val newLoan = lar.loan.copy(loanType = x)
      val newLar = lar.copy(loan = newLoan, purchaserType = y)
      newLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q035
}
