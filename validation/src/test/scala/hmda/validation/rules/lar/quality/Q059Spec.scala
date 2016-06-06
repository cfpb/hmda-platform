package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class Q059Spec extends LarEditCheckSpec with BadValueUtils {
  property("Lars with loan type not equal to 2, 3, or 4g must pass") {
    forAll(larGen, intOutsideRange(2, 4)) { (lar, x) =>
      val newLoan = lar.loan.copy(loanType = x)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  val relevantLoanType: Gen[Int] = Gen.choose(2, 4)

  property("Lar with relevant loan type and property type equal to 3 must fail") {
    forAll(larGen, relevantLoanType) { (lar, x) =>
      val newLoan = lar.loan.copy(loanType = x, propertyType = 3)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustFail
    }
  }

  val validPropertyType: Gen[Int] = intOtherThan(3)

  property("Lar with relevant loan type and property type not equal to 3 must pass") {
    forAll(larGen, relevantLoanType, validPropertyType) { (lar, x, y) =>
      val newLoan = lar.loan.copy(loanType = x, propertyType = y)
      val newLar = lar.copy(loan = newLoan)
      newLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q059
}
