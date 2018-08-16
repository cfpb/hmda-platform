package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V643Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V643

  property("If sex observed is true, sex must be male or female") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(
          sex = lar.applicant.sex.copy(sexObservedEnum = VisualOrSurnameSex)))

      val unapplicableLar = lar.copy(applicant = lar.applicant.copy(
        sex = lar.applicant.sex.copy(sexObservedEnum = NotVisualOrSurnameSex)))
      unapplicableLar.mustPass

      val sexMale = applicableLar.applicant.sex
        .copy(sexEnum = Male)
      val sexNotProvided = applicableLar.applicant.sex
        .copy(sexEnum = SexNotApplicable)
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexMale))
        .mustPass
      lar
        .copy(applicant = applicableLar.applicant.copy(sex = sexNotProvided))
        .mustFail
    }
  }
}
