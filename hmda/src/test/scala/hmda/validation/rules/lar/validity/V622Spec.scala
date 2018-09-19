package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V622Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V622

  property("If street is reported, then city, state, and zip must be reported") {
    forAll(larGen) { lar =>
      val unappLar = lar.copy(geography = lar.geography.copy(street = "NA"))
      unappLar.mustPass

      val appLar = lar.copy(geography = lar.geography.copy(street = "test"))
      appLar.copy(geography = appLar.geography.copy(state = "NA")).mustFail
      appLar.copy(geography = appLar.geography.copy(zipCode = "NA")).mustFail
      appLar
        .copy(
          geography = appLar.geography
            .copy(state = "test", zipCode = "test", city = "test"))
        .mustPass
    }
  }
}
