package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V631_4Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V631_4

  property("If ethnicity 1 is not available, ethnicity 2-5 must be blank") {
    forAll(larGen) { lar =>
      val applicableApplicant = lar.coApplicant.copy(
        ethnicity =
          lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(ethnicity =
          lar.coApplicant.ethnicity.copy(ethnicity1 = new InvalidEthnicityCode)))
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
        .copy(
          coApplicant = applicableApplicant.copy(ethnicity = ethnicityBlank))
        .mustPass
      lar
        .copy(
          coApplicant = applicableApplicant.copy(ethnicity = ethnicityNotBlank))
        .mustFail
    }
  }
}
