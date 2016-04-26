package hmda.validation.rules.lar.validity

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V285Spec extends LarEditCheckSpec {

  property("Succeeds when state is valid FIPS code") {
    forAll(larGen) { lar =>
      whenever(lar.geography.state != "NA") {
        V285(lar) mustBe a[Success]
      }
    }
  }

  property("Fails when state is invalid FIPS code") {
    forAll(larGen, badStateGen) { (lar, state) =>
      whenever(lar.geography.state != "NA") {
        val invalidGeography = lar.geography.copy(state = state)
        val invalidLar = lar.copy(geography = invalidGeography)
        V285(invalidLar) mustBe a[Failure]
      }
    }
  }

  property("Succeeds when state is NA and MSA/MD is NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(state = "NA", msa = "NA")
      val validLar = lar.copy(geography = validGeography)
      V285(validLar) mustBe a[Success]
    }
  }

  property("Fails when state is NA but MSA/MD is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val invalidGeography = lar.geography.copy(state = "11")
        val invalidLar = lar.copy(geography = invalidGeography)
        V285(invalidLar) mustBe a[Failure]
      }
    }
  }

  private def badStateGen: Gen[String] = Gen.choose(73, 99).toString

}
