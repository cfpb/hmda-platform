package hmda.validation.rules.lar.validity_2019

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.validity._2019.V648_2

class V648_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V648_2

  property(
    "If co-applicant sex is male and female, co-applicant sex observed must be not visual or surname") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexEnum = MaleAndFemale)))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexEnum = new InvalidSexCode)))
      unapplicableLar.mustPass

      val sexN = applicableLar.coApplicant.sex
        .copy(sexObservedEnum = NotVisualOrSurnameSex)
      val sexNotApp = applicableLar.coApplicant.sex
        .copy(sexObservedEnum = SexObservedNotApplicable)
      val sexNotProvided = applicableLar.coApplicant.sex
        .copy(sexObservedEnum = new InvalidSexObservedCode)
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexN))
        .mustPass
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexNotApp))
        .mustPass
      lar
        .copy(
          coApplicant = applicableLar.coApplicant.copy(sex = sexNotProvided))
        .mustFail
    }
  }
}
