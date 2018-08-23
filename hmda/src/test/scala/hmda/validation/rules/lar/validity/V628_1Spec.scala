package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V628_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V628_1

  property("If other ethnicity is blank, ") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(
          ethnicity = lar.applicant.ethnicity.copy(otherHispanicOrLatino = "")))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(ethnicity =
          lar.applicant.ethnicity.copy(otherHispanicOrLatino = "test")))
      unapplicableLar.mustPass

      val ethnicityValid = applicableLar.applicant.ethnicity
        .copy(ethnicity1 = HispanicOrLatino)
      val ethnicityInvalid = applicableLar.applicant.ethnicity
        .copy(ethnicity1 = InvalidEthnicityCode)
      lar
        .copy(
          applicant = applicableLar.applicant.copy(ethnicity = ethnicityValid))
        .mustPass
      lar
        .copy(applicant =
          applicableLar.applicant.copy(ethnicity = ethnicityInvalid))
        .mustFail
    }
  }
}
