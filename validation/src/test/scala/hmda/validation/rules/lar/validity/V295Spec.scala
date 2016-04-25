package hmda.validation.rules.lar.validity

import hmda.validation.dsl.Success
import hmda.validation.rules.lar.LarEditCheckSpec

class V295Spec extends LarEditCheckSpec {

  property("Succeeds for valid State/County combinations when MSA/MD is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val validGeography = lar.geography.copy(state = "06", county = "007")
        val validLar = lar.copy(geography = validGeography)
        V295(validLar) mustBe a[Success]
      }
    }
  }
}
