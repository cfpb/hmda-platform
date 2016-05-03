package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V525Spec extends LarEditCheckSpec with BadValueUtils {
  property("HOEPA status must be 1, or 2") {
    forAll(larGen) { lar =>
      whenever(lar.id == 2) {
        V525(lar) mustBe Success()
      }
    }
  }

  val badHoepaStatusGen: Gen[Int] = intOutsideRange(1, 2)

  property("HOEPA status other than 1, or 2 is invalid") {
    forAll(larGen, badHoepaStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(hoepaStatus = x)
      V525(invalidLar) mustBe Failure("is not contained in valid values domain")
    }
  }

}