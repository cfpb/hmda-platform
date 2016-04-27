package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.Geography
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec

class V300Spec extends LarEditCheckSpec {

  property("Succeeds for valid combination of MSA/MD, state, county and tract when MSA != NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("17020", "06", "007", "0036.00")
      val validLar = lar.copy(geography = validGeography)
      V300(validLar) mustBe a[Success]
    }
  }

  property("Fails for invalid combination of MSA/MD, state, county and tract when msa != NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        V300(lar) mustBe a[Failure]
      }
    }
  }
}
