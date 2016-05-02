package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V525Spec extends LarEditCheckSpec {
  property("HOEPA Type must = 1, or 2") {
    forAll(larGen) { lar =>
      V525(lar) mustBe Success()
    }
  }

  val badHoepaStatusGen: Gen[Int] = {
    val belowRange = Gen.choose(Integer.MIN_VALUE, 0)
    val aboveRange = Gen.choose(2, Integer.MAX_VALUE)
    Gen.oneOf(belowRange, aboveRange)
  }

  property("HOEPA status other than 1, or 2 is invalid") {
    forAll(larGen, badHoepaStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(hoepaStatus = x)
      V525(invalidLar) mustBe Failure("is not contained in valid values domain")
    }
  }

}