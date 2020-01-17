package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{EthnicityNoCoApplicant, _}
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V634Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V634

  property("If there is no coapplicant, observed must have no coapplicant") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          ethnicity = lar.coApplicant.ethnicity.copy(
            ethnicity1 = EthnicityNoCoApplicant,
            ethnicityObserved = EthnicityObservedNoCoApplicant)))
      passingLar.mustPass

      val invalidEthnicityLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          ethnicity = lar.coApplicant.ethnicity.copy(
            ethnicity1 = new InvalidEthnicityCode,
            ethnicityObserved = EthnicityObservedNoCoApplicant)))
      invalidEthnicityLar.mustFail

      val invalidEthnicityObservedLar = lar.copy(
        coApplicant = lar.coApplicant.copy(
          ethnicity = lar.coApplicant.ethnicity.copy(
            ethnicity1 = EthnicityNoCoApplicant,
            ethnicityObserved = new InvalidEthnicityObservedCode)))
      invalidEthnicityObservedLar.mustFail

      val passingLarWithCoApplicant = lar.copy(
        coApplicant = lar.coApplicant.copy(
          ethnicity = lar.coApplicant.ethnicity.copy(
            ethnicity1 = new InvalidEthnicityCode,
            ethnicityObserved = new InvalidEthnicityObservedCode)))
      passingLarWithCoApplicant.mustPass
    }
  }
}
