package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V667_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V667_2

  property(
    "If credit score type is other, other credit score model must be not empty") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType =
                                             OtherCreditScoreModel,
                                           otherCreditScoreModel = "test"))
      passingLar.mustPass

      val wrongCreditType = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType = EquifaxBeacon5,
                                           otherCreditScoreModel = "test"))
      wrongCreditType.mustFail

      val emptyOther = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType =
                                             OtherCreditScoreModel,
                                           otherCreditScoreModel = ""))
      emptyOther.mustFail

      val nonApplicable = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType = EquifaxBeacon5,
                                           otherCreditScoreModel = ""))
      nonApplicable.mustPass
    }
  }
}
