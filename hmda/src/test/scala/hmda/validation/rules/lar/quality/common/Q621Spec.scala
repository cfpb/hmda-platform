package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q621Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q621

  property("NMLSR Identifier IS Alpha numeric and has a lenghth of of 12 or less.") {
    forAll(larGen) { lar =>
     lar.mustPass

      val appLARTest= lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "1234TEST5678!!^"))
      appLARTest.mustFail


      val appLARTestTwo = lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "1234TEST512345666553"))
      appLARTestTwo.mustFail
    }

  }


}
