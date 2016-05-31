package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V295Spec extends LarEditCheckSpec {

  property("Succeeds for valid State/County combinations when MSA/MD is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val validGeography = lar.geography.copy(state = "06", county = "007")
        val validLar = lar.copy(geography = validGeography)
        validLar.mustPass
      }
    }
  }

  property("Fails for invalid State/County combinations when MSA/MD is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val invalidGeography = lar.geography.copy(state = "11", county = "555")
        val invalidLar = lar.copy(geography = invalidGeography)
        invalidLar.mustFail
      }
    }
  }

  property("Succeeds when county is NA and MSA/MD is also NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "NA", county = "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails when county is NA and MSA/MD is not NA") {
    forAll(larGen) { lar =>
      whenever(lar.geography.msa != "NA") {
        val invalidGeography = lar.geography.copy(county = "NA")
        val invalidLar = lar.copy(geography = invalidGeography)
        invalidLar.mustFail
      }
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V295
}
