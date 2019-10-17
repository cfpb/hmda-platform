package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.enums._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V714Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V714

  property(
    "If Application Channel exemption election is taken, all fields must be exempt") {
    forAll(larGen) { lar =>

      lar.copy(applicationSubmission = ApplicationSubmissionExempt, payableToInstitution = PayableToInstitutionNotApplicable).mustFail
      lar.copy(applicationSubmission = ApplicationSubmissionNotApplicable, payableToInstitution = PayableToInstitutionExempt).mustFail

      lar
        .copy(applicationSubmission = ApplicationSubmissionExempt,
              payableToInstitution = PayableToInstitutionExempt)
        .mustPass
    }
  }
}
