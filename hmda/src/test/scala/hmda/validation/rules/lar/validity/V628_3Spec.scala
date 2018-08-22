package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V628_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V628_3

  property(
    "If ethnicity is not applicable, ethnicity observed must not be applicable") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        applicant = lar.applicant.copy(ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable)))

      val unapplicableLar = lar.copy(
        applicant = lar.applicant.copy(ethnicity =
          lar.applicant.ethnicity.copy(ethnicity1 = InvalidEthnicityCode)))
      unapplicableLar.mustPass

      val ethnicityNA = applicableLar.applicant.ethnicity
        .copy(ethnicityObserved = EthnicityObservedNotApplicable)
      val ethnicityVis = applicableLar.applicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)
      lar
        .copy(applicant = applicableLar.applicant.copy(ethnicity = ethnicityNA))
        .mustPass
      lar
        .copy(
          applicant = applicableLar.applicant.copy(ethnicity = ethnicityVis))
        .mustFail
    }
  }
}
