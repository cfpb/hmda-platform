package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V695_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V695_1

  property("NMLSR ID should be numeric, Exempt, or NA") {
    forAll(larGen) { lar =>
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "NA")).mustPass
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "Exempt")).mustPass
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "123")).mustPass
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "na")).mustFail
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "exempt")).mustFail
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "123myId")).mustFail
      lar.copy(larIdentifier = lar.larIdentifier.copy(NMLSRIdentifier = "12.3")).mustFail
    }
  }
}