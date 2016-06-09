package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }

class Q029Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds for valid combination of MD, state, county and tract") {
    forAll(larGen) { lar =>
      val validGeography = Geography("14454", "25", "025", "0001.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails when combo has MSA but MSA == NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "06", "007", "0036.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustFail
    }
  }

  property("Succeeds when MSA is NA and the geo combo does not have an MSA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "56", "031", "9591.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  override def check: EditCheck[LoanApplicationRegister] = Q029
}
