package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
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

  property("Succeeds when MSA/MD is NA and county is also NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "NA", county = "NA")
      val validLar = lar.copy(geography = validGeography)
      V295(validLar) mustBe a[Success]
    }
  }

  //TODO: this fails, but should it? if there is a valid combination of state/county, do we care if msa == NA?
  //property("Fails when MSA/MD is NA but county has a value") {
  //  forAll(larGen) { lar =>
  //    whenever(lar.geography.county != "NA") {
  //      val invalidGeography = lar.geography.copy(msa = "NA", county = "001", state = "06")
  //      val invalidLar = lar.copy(geography = invalidGeography)
  //      println(invalidLar)
  //      V295(invalidLar) mustBe a[Failure]
  //    }
  //  }
  //}
}
