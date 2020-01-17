package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidApplicationSubmissionCode
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V693_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V693_1

  property("Submission of application must be valid") {
    forAll(larGen) { lar =>
      lar.mustPass
      val invalidLar =
        lar.copy(applicationSubmission = new InvalidApplicationSubmissionCode)
      invalidLar.mustFail
    }
  }
}
