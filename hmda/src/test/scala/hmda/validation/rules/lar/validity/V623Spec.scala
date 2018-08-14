package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V623Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V623

  property("State must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val badGeo = lar.geography.copy(state = "")
      lar.copy(geography = badGeo).mustFail
    }
  }
}
