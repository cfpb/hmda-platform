package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec
import hmda.validation.rules.lar.quality.common.Q642_1

class Q642_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q642_1

  property("If credit score is non a number, model should be other") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.creditScore != 7777) {
        lar.mustPass
      }

      val appLar =
        lar.copy(applicant = lar.applicant.copy(creditScore = 7777))
      appLar
        .copy(
          applicant = appLar.applicant.copy(creditScoreType = EquifaxBeacon5))
        .mustFail
      appLar
        .copy(
          applicant =
            appLar.applicant.copy(creditScoreType = OneOrMoreCreditScoreModels))
        .mustPass
    }
  }
}
