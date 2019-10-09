package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.quality.common.Q621

class Q621Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q621

  property("NMLSR Identifier should alphanumeric and not be longer than 12 characters") {
    forAll(larGen) { lar =>
     lar.mustPass

      val appLAR= lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "1234567890a!"))
      appLAR.mustFail
    }

  }


}
