package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V633Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V633

  property(
    "If ethnicity is not applicable, ethnicity observed must not be applicable") {
    forAll(larGen) { lar =>
      val applicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(ethnicity =
          lar.coApplicant.ethnicity.copy(ethnicity1 = EthnicityNotApplicable)))

      val unapplicableLar = lar.copy(
        coApplicant = lar.coApplicant.copy(ethnicity =
          lar.coApplicant.ethnicity.copy(ethnicity1 = new InvalidEthnicityCode)))
      unapplicableLar.mustPass

      val ethnicityNA = applicableLar.coApplicant.ethnicity
        .copy(ethnicityObserved = EthnicityObservedNotApplicable)
      val ethnicityVis = applicableLar.coApplicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)
      lar
        .copy(
          coApplicant = applicableLar.coApplicant.copy(ethnicity = ethnicityNA))
        .mustPass
      lar
        .copy(coApplicant =
          applicableLar.coApplicant.copy(ethnicity = ethnicityVis))
        .mustFail
    }
  }
}
