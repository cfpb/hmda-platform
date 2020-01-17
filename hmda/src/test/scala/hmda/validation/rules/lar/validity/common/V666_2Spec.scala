package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V666_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V666_2

  property("If there is no coapplicant, credit score must have no coapplicant") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 1, creditScoreType = new InvalidCreditScoreCode))
      passingLar.mustPass

      val creditScoreNALar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 9999, creditScoreType = new InvalidCreditScoreCode))
      creditScoreNALar.mustFail

      val creditScoreTypeNALar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 1, creditScoreType = CreditScoreNoCoApplicant))
      creditScoreTypeNALar.mustFail

      val passingLarWithCoApplicant = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 9999, creditScoreType = CreditScoreNoCoApplicant))
      passingLarWithCoApplicant.mustPass
    }
  }
}
