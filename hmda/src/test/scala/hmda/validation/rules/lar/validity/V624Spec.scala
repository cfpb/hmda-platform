package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V624Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V624

  property("Zip code must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val badGeo = lar.geography.copy(zipCode = "")
      lar.copy(geography = badGeo).mustFail
    }
  }
}
