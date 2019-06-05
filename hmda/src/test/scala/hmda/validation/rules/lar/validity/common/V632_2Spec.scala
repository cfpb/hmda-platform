package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V632_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V632_2
  property(
    "If Coapplicant Ethnicity Observed, Ethnicity Must Only be Hispanic of Not Hispanic") {
    forAll(larGen) { lar =>
      val relevantEthnicityObserved = lar.coApplicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)
      val irrelevantEthnicityObserved = lar.coApplicant.ethnicity
        .copy(ethnicityObserved = NotVisualOrSurnameEthnicity)
      val validEthnicity = relevantEthnicityObserved.copy(
        ethnicity1 = HispanicOrLatino,
        ethnicity2 = NotHispanicOrLatino,
        ethnicity3 = EmptyEthnicityValue,
        ethnicity4 = EmptyEthnicityValue,
        ethnicity5 = EmptyEthnicityValue
      )
      val invalidEthnicity = relevantEthnicityObserved.copy(
        ethnicity1 = EmptyEthnicityValue,
        ethnicity2 = EmptyEthnicityValue,
        ethnicity3 = EmptyEthnicityValue,
        ethnicity4 = EmptyEthnicityValue,
        ethnicity5 = EmptyEthnicityValue
      )
      val validLar1 =
        lar.copy(
          coApplicant =
            lar.coApplicant.copy(ethnicity = irrelevantEthnicityObserved))
      validLar1.mustPass
      val validLar2 =
        lar.copy(coApplicant = lar.coApplicant.copy(ethnicity = validEthnicity))
      validLar2.mustPass
      val invalidLar =
        lar.copy(
          coApplicant = lar.coApplicant.copy(ethnicity = invalidEthnicity))
      invalidLar.mustFail
    }
  }
}
