package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V290Spec extends LarEditCheckSpec {

  property("Succeeds when MSA == NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds when valid MSA, state and county codes are present") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "17020", county = "007", state = "06")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails for invalid combination of MSA, state and county") {
    forAll(larGen) { lar =>
      val invalidGeography = lar.geography.copy(msa = "17020", county = "001", state = "05")
      val invalidLar = lar.copy(geography = invalidGeography)
      invalidLar.mustFail
    }
  }

  property("Succeeds when valid MD, state and county codes are present") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "48424", state = "12", county = "099")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails for invalid combination of MD, state and county") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "48424", state = "12", county = "086")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V290
}
