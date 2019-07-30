package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V660_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V660_1

  property("Credit score must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }
}
