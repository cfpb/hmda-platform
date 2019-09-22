package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.NotPrimarilyBusinessOrCommercialPurpose
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q620Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q620

  property("Non-business loans should have an NMLSR ID") {
    forAll(larGen) { lar =>
      whenever(
        lar.businessOrCommercialPurpose != NotPrimarilyBusinessOrCommercialPurpose) {
        lar.mustPass
      }

      val appLar = lar.copy(
        businessOrCommercialPurpose = NotPrimarilyBusinessOrCommercialPurpose)
      appLar
        .copy(larIdentifier = appLar.larIdentifier.copy(NMLSRIdentifier = "NA"))
        .mustFail
      appLar
        .copy(larIdentifier = appLar.larIdentifier.copy(NMLSRIdentifier = "1"))
        .mustPass
    }
  }
}
