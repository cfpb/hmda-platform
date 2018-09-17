package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q621Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q621

  property("NMLSR Identifier should not be longer than 12 characters") {
    forAll(larGen) { lar =>
      whenever(lar.larIdentifier.NMLSRIdentifier.length <= 12) {
        lar.mustPass
      }

      lar
        .copy(
          larIdentifier =
            lar.larIdentifier.copy(NMLSRIdentifier = "1234567890abc"))
        .mustFail
    }
  }
}
