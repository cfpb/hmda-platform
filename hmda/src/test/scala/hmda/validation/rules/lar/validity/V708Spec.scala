package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidBusinessOrCommercialBusinessCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V708Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V708

  property("When AUS is Blank AUS Result must be blank") {
    forAll(larGen) { lar =>
      lar.mustPass
      lar
        .copy(
          businessOrCommercialPurpose = InvalidBusinessOrCommercialBusinessCode)
        .mustFail
    }
  }
}
