package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V666_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V666_1

  property("If credit score is NA, credit score model must be NA") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 1, creditScoreType = new InvalidCreditScoreCode))
      passingLar.mustPass

      val creditScoreNALar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 8888, creditScoreType = new InvalidCreditScoreCode))
      creditScoreNALar.mustFail

      val creditScoreTypeNALar = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 1, creditScoreType = CreditScoreNotApplicable))
      creditScoreTypeNALar.mustFail

      val passingLarWithCoApplicant = lar.copy(
        coApplicant = lar.coApplicant
          .copy(creditScore = 8888, creditScoreType = CreditScoreNotApplicable))
      passingLarWithCoApplicant.mustPass
    }
  }
}
