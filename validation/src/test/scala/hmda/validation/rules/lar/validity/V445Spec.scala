package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V445Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid when preapprovals not 2") {
    forAll(larGen, intOtherThan(2)) { (lar, preapp) =>
      val validLar = lar.copy(preapprovals = preapp)
      validLar.mustPass
    }
  }

  property("Valid when action taken in 1-5") {
    forAll(larGen, Gen.choose(1, 5)) { (lar, action) =>
      val validLar = lar.copy(actionTakenType = action)
      validLar.mustPass
    }
  }

  property("Invalid when action other than 1 to 5 and preapproval is 2") {
    forAll(larGen, intOtherThan(1 to 5)) { (lar, actionTaken) =>
      val invalidLar = lar.copy(preapprovals = 2, actionTakenType = actionTaken)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V445
}
