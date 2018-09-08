package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V632_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V632_3
  property(
    "If Coapplicant Ethnicity not Observed There must be a Valid Value for Ethnicity") {
    forAll(larGen) { lar =>
      val relevantEthnicityObserved = lar.coApplicant.ethnicity
        .copy(ethnicityObserved = NotVisualOrSurnameEthnicity)
      val irrelevantEthnicityObserved = lar.coApplicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)
      val validOtherEthnicity =
        lar.coApplicant.ethnicity.copy(otherHispanicOrLatino = "other")
      val validEthnicity1 = relevantEthnicityObserved.copy(
        ethnicity1 = HispanicOrLatino
      )
      val invalidEthnicity = relevantEthnicityObserved.copy(
        ethnicity1 = EmptyEthnicityValue,
        otherHispanicOrLatino = ""
      )
      val validLar1 =
        lar.copy(
          coApplicant =
            lar.coApplicant.copy(ethnicity = irrelevantEthnicityObserved))
      validLar1.mustPass
      val validLar2 =
        lar.copy(
          coApplicant = lar.coApplicant.copy(ethnicity = validEthnicity1))
      validLar2.mustPass
      val validLar3 = lar.copy(
        coApplicant = lar.coApplicant.copy(ethnicity = validOtherEthnicity))
      validLar3.mustPass
      val invalidLar =
        lar.copy(
          coApplicant = lar.coApplicant.copy(ethnicity = invalidEthnicity))
      invalidLar.mustFail
    }
  }
}
