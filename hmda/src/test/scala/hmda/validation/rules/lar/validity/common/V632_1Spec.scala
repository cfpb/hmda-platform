package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V632_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V632_1
  property("The Ethnicity of Coapplication Observed Code must be Valid") {
    forAll(larGen) { lar =>
      val invalidEthnicity1 = lar.coApplicant.ethnicity
        .copy(ethnicityObserved = new InvalidEthnicityObservedCode)
      val validEthnicity = lar.coApplicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)
      val invalidLar1 =
        lar.copy(
          coApplicant = lar.coApplicant.copy(ethnicity = invalidEthnicity1))
      invalidLar1.mustFail
      val validLar =
        lar.copy(coApplicant = lar.coApplicant.copy(ethnicity = validEthnicity))
      validLar.mustPass
    }
  }
}
