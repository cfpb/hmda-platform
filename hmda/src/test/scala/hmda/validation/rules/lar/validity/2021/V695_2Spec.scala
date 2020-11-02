package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V695_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V695_2

  property("NMLSR ID should not be 0") {
    forAll(larGen) { lar =>
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "1")).mustPass
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "00")).mustPass
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "0")).mustFail
    }
  }
}