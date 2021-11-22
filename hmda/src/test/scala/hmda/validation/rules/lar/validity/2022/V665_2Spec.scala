package hmda.validation.rules.lar.validity._2022

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V665_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V665_2

  property("Co-applicant Credit score type must be valid") {
    forAll(larGen) { lar =>
      lar
        .copy(
          coApplicant =
            lar.coApplicant.copy(creditScoreType = FICORiskScoreClassic98 ))
        .mustPass

      lar
        .copy(
          coApplicant =
            lar.coApplicant.copy(creditScoreType =  VantageScore2 ))
        .mustPass

      lar
        .copy(
          coApplicant =
            lar.coApplicant.copy(creditScoreType = OtherCreditScoreModel ))
        .mustPass


      lar
        .copy(
          coApplicant =
            lar.coApplicant.copy(creditScoreType = new InvalidCreditScoreCode))
        .mustFail
    }
  }
}