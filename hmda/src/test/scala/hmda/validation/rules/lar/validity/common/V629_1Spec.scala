package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V629_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V629_1

  property("The Ethnicity Observed Code must be Valid") {
    forAll(larGen) { lar =>
      val invalidEthnicity1 = lar.applicant.ethnicity
        .copy(ethnicityObserved = new InvalidEthnicityObservedCode)
      val invalidEthnicity2 = lar.applicant.ethnicity
        .copy(ethnicityObserved = EthnicityObservedNoCoApplicant)
      val validEthnicity = lar.applicant.ethnicity
        .copy(ethnicityObserved = VisualOrSurnameEthnicity)

      val invalidLar1 =
        lar.copy(applicant = lar.applicant.copy(ethnicity = invalidEthnicity1))
      invalidLar1.mustFail

      val invalidLar2 =
        lar.copy(applicant = lar.applicant.copy(ethnicity = invalidEthnicity2))
      invalidLar2.mustFail

      val validLar =
        lar.copy(applicant = lar.applicant.copy(ethnicity = validEthnicity))
      validLar.mustPass
    }
  }
}
