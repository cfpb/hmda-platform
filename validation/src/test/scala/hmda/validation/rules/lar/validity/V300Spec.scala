package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.Geography
import hmda.validation.dsl.Success
import hmda.validation.rules.lar.LarEditCheckSpec

class V300Spec extends LarEditCheckSpec {

  property("Succeeds for valid combination of MSA/MD, state, county and tract") {
    forAll(larGen) { lar =>
      val validGeography = Geography("17020", "06", "06007", "003600")
      val validLar = lar.copy(geography = validGeography)
      println(validLar)
      V300(validLar) mustBe a[Success]
    }
  }
}
