package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V664Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V664

  property(
    "If application is closed, co-applicant credit score model must be empty or exempt") {
    forAll(larGen) { lar =>
      lar
        .copy(action = lar.action.copy(actionTakenType = ApplicationDenied))
        .mustPass

      val appLar = lar.copy(
        action =
          lar.action.copy(actionTakenType = ApplicationWithdrawnByApplicant))

      appLar
        .copy(coApplicant = appLar.coApplicant.copy(creditScore = 9))
        .mustFail
      appLar
        .copy(coApplicant =
          appLar.coApplicant.copy(creditScoreType = VantageScore3))
        .mustFail
      appLar
        .copy(
          coApplicant = appLar.coApplicant.copy(otherCreditScoreModel = "test"))
        .mustFail
      appLar
        .copy(
          coApplicant = appLar.coApplicant.copy(creditScore = 1111,
                                                creditScoreType =
                                                  CreditScoreExempt,
                                                otherCreditScoreModel = ""))
        .mustPass
    }
  }
}
