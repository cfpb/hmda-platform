package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V280Spec extends LarEditCheckSpec {

  property("Succeeds for valid MSA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "17020")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds for valid MD") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "48424")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds for NA") {
    forAll(larGen) { lar =>
      val validGeography = lar.geography.copy(msa = "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails for invalid MSA code") {
    forAll(larGen) { lar =>
      val inValidGeography = lar.geography.copy(msa = "010201")
      val inValidLar = lar.copy(geography = inValidGeography)
      inValidLar.mustFail
    }
  }

  property("Fails for a blank MSA code") {
    forAll(larGen) { lar =>
      val inValidGeography = lar.geography.copy(msa = "")
      val inValidLar = lar.copy(geography = inValidGeography)
      inValidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V280
}
