package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V662_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V662_2

  property(
    "If credit score type is other, other credit score model must be not empty") {
    forAll(larGen) { lar =>
      val passingLar = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = OtherCreditScoreModel,
                                       otherCreditScoreModel = "test"))
      passingLar.mustPass

      val wrongCreditType = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = EquifaxBeacon5,
                                       otherCreditScoreModel = "test"))
      wrongCreditType.mustFail

      val emptyOther = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = OtherCreditScoreModel,
                                       otherCreditScoreModel = ""))
      emptyOther.mustFail

      val nonApplicable = lar.copy(
        applicant = lar.applicant.copy(creditScoreType = EquifaxBeacon5,
                                       otherCreditScoreModel = ""))
      nonApplicable.mustPass
    }
  }
}
