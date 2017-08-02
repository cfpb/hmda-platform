package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }
import org.scalacheck.Gen

class V447Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid when preapprovals not 3") {
    forAll(larGen, intOtherThan(3)) { (lar, preapp) =>
      val validLar = lar.copy(preapprovals = preapp)
      validLar.mustPass
    }
  }

  property("Valid when action taken in 1-6") {
    forAll(larGen, Gen.choose(1, 6)) { (lar, action) =>
      val validLar = lar.copy(actionTakenType = action)
      validLar.mustPass
    }
  }

  property("Invalid when action other than 1 to 6 and preapproval is 3") {
    forAll(larGen, intOtherThan(1 to 6)) { (lar, actionTaken) =>
      val invalidLar = lar.copy(preapprovals = 3, actionTakenType = actionTaken)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V447
}
