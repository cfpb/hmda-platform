package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V663Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V663

  property(
    "If application is closed, credit score model must be empty or exempt") {
    forAll(larGen) { lar =>
      lar
        .copy(action = lar.action.copy(actionTakenType = ApplicationDenied))
        .mustPass

      val appLar = lar.copy(
        action =
          lar.action.copy(actionTakenType = ApplicationWithdrawnByApplicant))

      appLar.copy(applicant = appLar.applicant.copy(creditScore = 9)).mustFail
      appLar
        .copy(
          applicant = appLar.applicant.copy(creditScoreType = VantageScore3))
        .mustFail
      appLar
        .copy(applicant = appLar.applicant.copy(otherCreditScoreModel = "test"))
        .mustFail
      appLar
        .copy(
          applicant = appLar.applicant.copy(creditScore = 1111,
                                            creditScoreType = CreditScoreExempt,
                                            otherCreditScoreModel = ""))
        .mustPass
    }
  }
}
