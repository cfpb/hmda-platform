package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V667_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V667_1

  property("If credit score type is valid, other credit score must be blank") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType = EquifaxBeacon5,
                                           otherCreditScoreModel = ""))
      passingLar.mustPass

      val wrongCreditType = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType =
                                             OtherCreditScoreModel,
                                           otherCreditScoreModel = ""))
      wrongCreditType.mustFail

      val nonEmptyOther = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType = EquifaxBeacon5,
                                           otherCreditScoreModel = "test"))
      nonEmptyOther.mustFail

      val nonApplicable = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType =
                                             OtherCreditScoreModel,
                                           otherCreditScoreModel = "test"))
      nonApplicable.mustPass
    }
  }
}
