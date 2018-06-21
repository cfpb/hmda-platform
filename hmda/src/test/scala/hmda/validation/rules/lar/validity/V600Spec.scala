package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.{LarIdentifier, LoanApplicationRegister}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._

class V600Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V600

  property("LEI in LAR must be 20 characters long") {
    forAll(larGen) { lar =>
      whenever(lar.larIdentifier.LEI != "") {
        lar.mustPass
      }
      lar.copy(larIdentifier = LarIdentifier(2, "", "")).mustFail
      lar.copy(larIdentifier = LarIdentifier(2, "", "ABCD")).mustFail
    }
  }
}
