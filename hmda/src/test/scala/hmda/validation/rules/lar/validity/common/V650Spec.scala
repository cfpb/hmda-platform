package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V650Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V650

  property(
    "If co-applicant sex is no co-applicant, co-applicant observed sex must be no co-applicant") {
    forAll(larGen) { lar =>
      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexEnum = new InvalidSexCode,
                                         sexObservedEnum =
                                           new InvalidSexObservedCode)))
      unapplicableLar.mustPass

      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          sex = lar.coApplicant.sex.copy(sexEnum = SexNoCoApplicant,
                                         sexObservedEnum =
                                           SexObservedNoCoApplicant)))
      applicableLar.mustPass

      val sexOInvalid = applicableLar.coApplicant.sex
        .copy(sexObservedEnum = new InvalidSexObservedCode)
      val sexInvalid = applicableLar.coApplicant.sex
        .copy(sexEnum = new InvalidSexCode)

      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexOInvalid))
        .mustFail
      lar
        .copy(coApplicant = applicableLar.coApplicant.copy(sex = sexInvalid))
        .mustFail
    }
  }
}
