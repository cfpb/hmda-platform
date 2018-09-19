package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V665_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V665_1

  property("Co-applicant Credit score must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
    }
  }
}
