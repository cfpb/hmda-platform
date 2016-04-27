package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec

class V280Spec extends LarEditCheckSpec {

  property("Succeeds for valid MSA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "17020")
      val validLar = lar.copy(geography = validGeography)
      V280(validLar) mustBe a[Success]
    }
  }

  property("Succeeds for valid MD") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "48424")
      val validLar = lar.copy(geography = validGeography)
      V280(validLar) mustBe a[Success]
    }
  }

  property("Succeeds for NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "NA")
      val validLar = lar.copy(geography = validGeography)
      V280(validLar) mustBe a[Success]
    }
  }

  property("Fails for invalid MSA code") {
    forAll(larGen) { lar =>
      val inValidGeography = lar.geography.copy(msa = "010201")
      val inValidLar = lar.copy(geography = inValidGeography)
      V280(inValidLar) mustBe a[Failure]
    }
  }

}
