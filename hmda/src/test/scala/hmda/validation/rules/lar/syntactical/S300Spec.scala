package hmda.validation.rules.lar.syntactical

import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.validation.rules.lar.syntactical._2018.S300

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
