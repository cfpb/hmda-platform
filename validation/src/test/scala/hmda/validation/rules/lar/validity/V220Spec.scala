package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.parser.fi.lar.LarGenerators
import hmda.validation.dsl.{ Failure, Success }
import org.scalacheck.Gen
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class V220Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("Loan Type must = 1,2,3, or 4") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        V220(lar.loan) mustBe Success()
      }
    }
  }

  val badLoanTypeGen: Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, 0)
    val aboveRange = Gen.choose(5, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  property("Loan Type other than 1,2,3,4 is invalid") {
    forAll(larGen, badLoanTypeGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan: Loan = lar.loan.copy(loanType = x)
      V220(invalidLoan) mustBe Failure("is not contained in valid values domain")
    }
  }

}
