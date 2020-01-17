package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V649Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V649

  property("If co-applicant sex is NA, co-applicant observed sex must be NA") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexEnum = SexNotApplicable)))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexEnum = new InvalidSexCode)))
      unapplicableLar.mustPass

      val sexNA = applicableLar.coApplicant.sex
        .copy(sexObservedEnum = SexObservedNotApplicable)
      val sexNotProvided = applicableLar.coApplicant.sex
        .copy(sexObservedEnum = new InvalidSexObservedCode)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexNA))
        .mustPass
      lar
        .copy(
          coApplicant = applicableLar.coApplicant.copy(sex = sexNotProvided))
        .mustFail
    }
  }
}
