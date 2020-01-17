package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V628_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V628_4

  property("If ethnicity 1 is not available, ethnicity 2-5 must be blank") {
    forAll(larGen) { lar =>
      val applicableApplicant = lar.applicant.copy(
        ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = new InvalidEthnicityCode)))
      unapplicableLar.mustPass

      val ethnicityBlank =
        applicableApplicant.ethnicity.copy(ethnicity2 = EmptyEthnicityValue,
                                           ethnicity3 = EmptyEthnicityValue,
                                           ethnicity4 = EmptyEthnicityValue,
                                           ethnicity5 = EmptyEthnicityValue)

      val ethnicityNotBlank =
        applicableApplicant.ethnicity.copy(ethnicity2 = EmptyEthnicityValue,
                                           ethnicity3 = new InvalidEthnicityCode,
                                           ethnicity4 = EmptyEthnicityValue,
                                           ethnicity5 = new InvalidEthnicityCode)

      lar
        .copy(applicant = applicableApplicant.copy(ethnicity = ethnicityBlank))
        .mustPass
      lar
        .copy(
          applicant = applicableApplicant.copy(ethnicity = ethnicityNotBlank))
        .mustFail
    }
  }
}
