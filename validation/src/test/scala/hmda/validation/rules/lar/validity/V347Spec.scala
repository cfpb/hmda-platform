package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V347Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds when there is a relevant purchaser type (1-9) and loan was originated or purchased (1 or 6)") {
    forAll(larGen, Gen.choose(1, 9), Gen.oneOf(1, 6)) { (lar, pt, action) =>
      val newLar = lar.copy(purchaserType = pt, actionTakenType = action)
      newLar.mustPass
    }
  }

  property("Fails when there is a relevant purchaser type (1-9) but loan was not originated (1) nor purchased (6)") {
    forAll(larGen, Gen.choose(1, 9)) { (lar, pt) =>
      whenever(!List(1, 6).contains(lar.actionTakenType)) {
        val newLar = lar.copy(purchaserType = pt)
        newLar.mustFail
      }
    }
  }

  property("Succeeds when Type of Purchaser = 0") {
    forAll(larGen) { lar =>
      lar.copy(purchaserType = 0).mustPass
    }
  }

  property("Succeeds when Type of Purchaser is invalid") {
    forAll(badPurchaserTypeLarGen) { lar =>
      lar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V347
}
