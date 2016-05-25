package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V440Spec extends LarEditCheckSpec with BadValueUtils {

  property("Valid when preapprovals not 1") {
    forAll(larGen, intOtherThan(1)) { (lar, preapp) =>
      val validLar = lar.copy(preapprovals = preapp)
      validLar.mustPass
    }
  }

  property("Valid when action taken in 1-5, 7, or 8") {
    forAll(larGen, Gen.oneOf((1 to 5) ++ (7 to 8))) { (lar, action) =>
      val validLar = lar.copy(actionTakenType = action)
      validLar.mustPass
    }
  }

  property("Invalid when action other than 1 to 5, 7, or 8 and preapproval is 1") {
    forAll(larGen, intOtherThan((1 to 5) ++ (7 to 8))) { (lar, actionTaken) =>
      val invalidLar = lar.copy(preapprovals = 1, actionTakenType = actionTaken)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V440
}
