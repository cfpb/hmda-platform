package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V662_1Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V662_1

  property("If credit score type is valid, other credit score must be blank") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = EquifaxBeacon5,
          otherCreditScoreModel = ""))
      passingLar.mustPass
      val otherPassingLar = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = FICOScore9,
          otherCreditScoreModel = ""))
      otherPassingLar.mustPass

      val wrongCreditType = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = OtherCreditScoreModel,
          otherCreditScoreModel = ""))
      wrongCreditType.mustFail

      val nonEmptyOther = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = EquifaxBeacon5,
          otherCreditScoreModel = "test"))
      nonEmptyOther.mustFail

      val nonApplicable = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = OtherCreditScoreModel,
          otherCreditScoreModel = "test"))
      nonApplicable.mustPass
    }
  }
}
