package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V631_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V631_2

  property("Ethnicities 2-5 must contian valid values") {
    forAll(larGen) { lar =>
      val validEthnicity =
        lar.coApplicant.ethnicity.copy(ethnicity2 = EmptyEthnicityValue,
                                       ethnicity3 = EmptyEthnicityValue,
                                       ethnicity4 = EmptyEthnicityValue,
                                       ethnicity5 = EmptyEthnicityValue)

      val invalidEthnicity =
        lar.coApplicant.ethnicity.copy(ethnicity2 = EmptyEthnicityValue,
                                       ethnicity3 = EthnicityNoCoApplicant,
                                       ethnicity4 = new InvalidEthnicityCode,
                                       ethnicity5 = EmptyEthnicityValue)

      val validLar =
        lar.copy(coApplicant = lar.coApplicant.copy(ethnicity = validEthnicity))
      validLar.mustPass

      val invalidLar =
        lar.copy(
          coApplicant = lar.coApplicant.copy(ethnicity = invalidEthnicity))
      invalidLar.mustFail
    }
  }
}
