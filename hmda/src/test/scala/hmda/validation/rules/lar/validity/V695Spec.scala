package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V695Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V695

  property("NMLSR Identifier cannot be blank") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar =
        lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = ""))
      invalidLar.mustFail
    }
  }
}
