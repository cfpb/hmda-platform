package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators.larGen
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V629_3Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V629_3

  property(
    "If Ethnicity not Observed There must be a Valid Value for Ethnicity") {
    forAll(larGen) { lar =>
      val relevantEthnicityObserved = lar.applicant.ethnicity
        .copy(ethnicityObserved = NotVisualOrSurnameEthnicity)
      val irrelevantEthnicityObserved = lar.applicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)

      val validOtherEthnicity =
        lar.applicant.ethnicity.copy(otherHispanicOrLatino = "other")

      val validEthnicity1 = relevantEthnicityObserved.copy(
        ethnicity1 = HispanicOrLatino
      )

      val invalidEthnicity = relevantEthnicityObserved.copy(
        ethnicity1 = EmptyEthnicityValue,
        otherHispanicOrLatino = ""
      )

      val validLar1 =
        lar.copy(
          applicant =
            lar.applicant.copy(ethnicity = irrelevantEthnicityObserved))
      validLar1.mustPass

      val validLar2 =
        lar.copy(applicant = lar.applicant.copy(ethnicity = validEthnicity1))
      validLar2.mustPass

      val validLar3 = lar.copy(
        applicant = lar.applicant.copy(ethnicity = validOtherEthnicity))
      validLar3.mustPass

      val invalidLar =
        lar.copy(applicant = lar.applicant.copy(ethnicity = invalidEthnicity))
      invalidLar.mustFail

    }
  }
}
