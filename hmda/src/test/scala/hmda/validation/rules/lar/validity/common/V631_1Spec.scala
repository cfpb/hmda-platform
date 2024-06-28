package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V631_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V631_1


  property("If other ethnicity is blank, an ethnicity must be provided") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(coApplicant = lar.coApplicant.copy(
        ethnicity = lar.coApplicant.ethnicity.copy(otherHispanicOrLatino = "")))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(ethnicity =
          lar.coApplicant.ethnicity.copy(otherHispanicOrLatino = "test"))) //if free form is not blank and ethnicity is provided correctly
      unapplicableLar.mustPass

      val ethnicityValid = applicableLar.coApplicant.ethnicity
        .copy(ethnicity1 = HispanicOrLatino)
      val ethnicityInvalid = applicableLar.coApplicant.ethnicity
        .copy(ethnicity1 = new InvalidEthnicityCode)
      lar
        .copy(
          coApplicant =
            applicableLar.coApplicant.copy(ethnicity = ethnicityValid)) // if free form is blank and ethnicity is provided correctly
        .mustPass
      lar
        .copy(
          coApplicant =
            applicableLar.coApplicant.copy(ethnicity = ethnicityInvalid)) // if free form is blank and ethnicity is provided incorrectly
        .mustFail
      lar
        .copy(
          coApplicant =
            unapplicableLar.coApplicant.copy(ethnicity = ethnicityInvalid)) // if free form is not blank and ethnicity is provided incorrectly
        .mustFail

      val ethnicityEmpty = applicableLar.coApplicant.ethnicity
        .copy(ethnicity1 = new InvalidEthnicityCode(0))
      lar
        .copy(
          coApplicant =
            applicableLar.coApplicant.copy(ethnicity = ethnicityEmpty)) // if free form is blank and ethnicity is blank
        .mustFail

    }
  }
}
