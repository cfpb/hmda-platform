package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V225Spec extends LarEditCheckSpec {
  property("Loan Purpose must = 1, 2, or 3") {
    forAll(larGen) { lar =>
      V225(lar) mustBe Success()
    }
  }

  val badLoanPurposeGen: Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, 0)
    val aboveRange = Gen.choose(4, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  property("Loan Purpose other than 1,2,3 is invalid") {
    forAll(larGen, badLoanPurposeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(purpose = x)
      val invalidLar: LoanApplicationRegister = lar.copy(loan = invalidLoan)
      V225(invalidLar) mustBe Failure("is not contained in valid values domain")
    }
  }

}
