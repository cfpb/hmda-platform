package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec

class V290Spec extends LarEditCheckSpec {

  property("Succeeds when valid MSA/MD, state and county codes are present") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val validGeography = lar.geography.copy(msa = "17020", county = "007", state = "06")
        val validLar = lar.copy(geography = validGeography)
        V290(validLar) mustBe a[Success]
      }
    }
  }

  property("Succeeds when MSA == NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "NA")
      val validLar = lar.copy(geography = validGeography)
      V290(validLar) mustBe a[Success]
    }
  }

  property("Succeeds when code is MD (not MSA) with valid state and county combination") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "48424", state = "12", county = "086")
      val validLar = lar.copy(geography = validGeography)
      V290(validLar) mustBe a[Success]
    }
  }

  property("Fails for invalid combination of MSA/MD, state and county") {
    forAll(larGen) { lar =>
      val invalidGeography = lar.geography.copy(msa = "17020")
      val invalidLar = lar.copy(geography = invalidGeography)
      V290(invalidLar) mustBe a[Failure]
    }
  }

}
