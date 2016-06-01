package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}
import org.scalacheck.Gen

class V230Spec extends LarEditCheckSpec with BadValueUtils {
  property("Occupancy must be 1, 2, 3") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badLienStatusGen: Gen[Int] = intOutsideRange(1, 3)

  property("Occupancy other than 1, 2, or 3 is invalid") {
    forAll(larGen, badLienStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLoan = lar.loan.copy(occupancy = x)
      val invalidLar = lar.copy(loan = invalidLoan)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V230
}
