package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import org.scalacheck.Gen

class V285Spec extends LarEditCheckSpec {

  property("Succeeds when state is valid FIPS code") {
    forAll(larGen) { lar =>
      whenever(lar.geography.state != "NA") {
        lar.mustPass
      }
    }
  }

  property("Fails when state is invalid FIPS code") {
    forAll(larGen, badStateGen) { (lar, state) =>
      whenever(lar.geography.state != "NA") {
        val invalidGeography = lar.geography.copy(state = state)
        val invalidLar = lar.copy(geography = invalidGeography)
        invalidLar.mustFail
      }
    }
  }

  property("Succeeds when state is NA and MSA/MD is NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(state = "NA", msa = "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails when state is NA but MSA/MD is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val invalidGeography = lar.geography.copy(state = "NA")
        val invalidLar = lar.copy(geography = invalidGeography)
        invalidLar.mustFail
      }
    }
  }

  private def badStateGen: Gen[String] = Gen.choose(73, 99).toString

  override def check: EditCheck[LoanApplicationRegister] = V285
}
