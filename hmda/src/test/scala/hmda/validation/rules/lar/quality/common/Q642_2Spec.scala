package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q642_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q642_2

  property("If credit score is non a number, model should be other") {
    forAll(larGen) { lar =>
      whenever(lar.coApplicant.creditScore != 7777) {
        lar.mustPass
      }

      val appLar =
        lar.copy(coApplicant = lar.coApplicant.copy(creditScore = 7777))
      appLar
        .copy(
          coApplicant =
            appLar.coApplicant.copy(creditScoreType = EquifaxBeacon5))
        .mustFail
      appLar
        .copy(coApplicant =
          appLar.coApplicant.copy(creditScoreType = OneOrMoreCreditScoreModels))
        .mustPass
    }
  }
}
