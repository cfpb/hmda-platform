package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V525Spec extends LarEditCheckSpec with BadValueUtils {
  property("HOEPA status must be 1, or 2") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badHoepaStatusGen: Gen[Int] = intOutsideRange(1, 2)

  property("HOEPA status other than 1, or 2 is invalid") {
    forAll(larGen, badHoepaStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(hoepaStatus = x)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V525
}
