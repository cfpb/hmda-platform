package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V400Spec extends LarEditCheckSpec {
  property("Property Type must = 1,2, or 3") {
    forAll(larGen) { lar =>
      V400(lar) mustBe Success()
    }
  }

  val badPropertyTypeGen: Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, 0)
    val aboveRange = Gen.choose(4, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  property("Property Type other than 1,2,3 is invalid") {
    forAll(larGen, badPropertyTypeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(propertyType = x)
      val invalidLar: LoanApplicationRegister = lar.copy(loan = invalidLoan)
      V400(invalidLar) mustBe a[Failure]
    }
  }

}
