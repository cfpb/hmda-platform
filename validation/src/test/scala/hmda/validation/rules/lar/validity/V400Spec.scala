package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V400Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
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
