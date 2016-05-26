package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V300Spec extends LarEditCheckSpec {

  property("Succeeds for valid combination of MSA, state, county and tract when MSA != NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("17020", "06", "007", "0036.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds for valid combination of MD, state, county and tract when MSA != NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("14454", "25", "025", "0001.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds for valid combination of MSA, state, county and tract when MSA == NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "06", "007", "0036.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds for valid combination of MD, state, county and tract when MSA == NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "25", "025", "0001.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds for MD, state, county and tract all = NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "NA", "NA", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails for valid MSA not NA in a small county") {
    forAll(larGen) { lar =>
      val invalidGeography = Geography("962100", "46", "045", "9621.00")
      val invalidLar = lar.copy(geography = invalidGeography)
      invalidLar.mustFail
    }
  }

  property("Succeeds for NA track in a small county") {
    forAll(larGen) { lar =>
      val validGeography = Geography("962100", "46", "045", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails for invalid combination of MSA/MD, state, county and tract when msa != NA") {
    forAll(larGen) { lar =>
      val inValidGeography = Geography("17021", "06", "007", "0606.00")
      val inValidLar = lar.copy(geography = inValidGeography)
      inValidLar.mustFail
    }
  }

  property("Fails for invalid combination of MSA/MD, state, county and tract when msa == NA") {
    forAll(larGen) { lar =>
      val inValidGeography = Geography("NA", "06", "007", "0606.00")
      val inValidLar = lar.copy(geography = inValidGeography)
      inValidLar.mustFail
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = V300
}
