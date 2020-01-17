package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V647Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V647

  property(
    "If co-applicant sex is collected on observation, co-applicant sex must be male or female") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexObservedEnum = VisualOrSurnameSex)))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(sex =
          lar.coApplicant.sex.copy(sexObservedEnum = new InvalidSexObservedCode)))
      unapplicableLar.mustPass

      val sexMale = applicableLar.coApplicant.sex
        .copy(sexEnum = Male)
      val sexNotProvided = applicableLar.coApplicant.sex
        .copy(sexEnum = new InvalidSexCode)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexMale))
        .mustPass
      lar
        .copy(
          coApplicant = applicableLar.coApplicant.copy(sex = sexNotProvided))
        .mustFail
    }
  }
}
