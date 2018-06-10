package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._

class S300Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = S300

  property("LAR record identifier must be 2") {
    forAll(larGen) { lar =>
      whenever(lar.larIdentifier.id == 2) {
        lar.mustPass
      }
    }
  }
}
