package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V415Spec extends LarEditCheckSpec with BadValueUtils {
  property("Preapprovals must be 1, 2, or 3") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }

  val invalidPreaprovalsGen: Gen[Int] = intOutsideRange(1, 3)

  property("Preapproval other than 1, 2, or 3 is invalid") {
    forAll(larGen, invalidPreaprovalsGen) { (lar: LoanApplicationRegister, x: Int) =>
      val invalidLar: LoanApplicationRegister = lar.copy(preapprovals = x)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V415
}
