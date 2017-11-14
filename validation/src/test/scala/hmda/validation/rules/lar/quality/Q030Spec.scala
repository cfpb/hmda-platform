package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class Q030Spec extends LarEditCheckSpec {
  property("passes when action take is not in range") {
    forAll(larGen, Gen.choose(7, 9)) { (lar, i) =>
      val validLar = lar.copy(actionTakenType = i)
      validLar.mustPass
    }
  }

  property("passes when action taken is in range and geography is not NA") {
    forAll(larGen, Gen.choose(1, 6), stringOfN(3, Gen.alphaChar)) { (lar, i, geo) =>
      val validGeo = Geography(geo, geo, geo, geo)
      val validLar = lar.copy(actionTakenType = i, geography = validGeo)
      validLar.mustPass
    }
  }

  property("fails when action taken is in range and geography is NA") {
    forAll(larGen, Gen.choose(1, 6)) { (lar, i) =>
      val invalidGeo = Geography("NA", "NA", "NA", "NA")
      val invalidLar = lar.copy(actionTakenType = i, geography = invalidGeo)
      invalidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q030
}
