package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{Loan, LoanApplicationRegister}
import hmda.validation.dsl.{Failure, Success}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{BadValueUtils, LarEditCheckSpec}
import org.scalacheck.Gen

class V550Spec extends LarEditCheckSpec with BadValueUtils {
  property("Lien Status must be 1, 2, 3, or 4") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val badLienStatusGen: Gen[Int] = intOutsideRange(1, 4)

  property("Lien status other than 1, 2, 3, or 4 is invalid") {
    forAll(larGen, badLienStatusGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar = lar.copy(lienStatus = x)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V550
}
