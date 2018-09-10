package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V648_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V648_1

  property(
    "If co-applicant sex is collected on observation, co-applicant sex must be male, female, not provided, or male and female") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(sex =
          lar.coApplicant.sex.copy(sexObservedEnum = NotVisualOrSurnameSex)))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(sex =
          lar.coApplicant.sex.copy(sexObservedEnum = InvalidSexObservedCode)))
      unapplicableLar.mustPass

      val sexM = applicableLar.coApplicant.sex
        .copy(sexEnum = MaleAndFemale)
      val sexNotProvided = applicableLar.coApplicant.sex
        .copy(sexEnum = InvalidSexCode)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexM))
        .mustPass
      lar
        .copy(
          coApplicant = applicableLar.coApplicant.copy(sex = sexNotProvided))
        .mustFail
    }
  }
}
