package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.{LarIdentifier, LoanApplicationRegister}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.model.filing.lar.LarGenerators._

class Q607Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q607

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
