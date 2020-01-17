package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V645Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V645

  property("If sex is not applicable, sex observed must not be applicable") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(
          sex = lar.applicant.sex.copy(sexEnum = SexNotApplicable)))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(
          sex = lar.applicant.sex.copy(sexEnum = new InvalidSexCode)))
      unapplicableLar.mustPass

      val sexNA = applicableLar.applicant.sex
        .copy(sexObservedEnum = SexObservedNotApplicable)
      val sexNotProvided = applicableLar.applicant.sex
        .copy(sexObservedEnum = new InvalidSexObservedCode)
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexNA))
        .mustPass
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexNotProvided))
        .mustFail
    }
  }
}
