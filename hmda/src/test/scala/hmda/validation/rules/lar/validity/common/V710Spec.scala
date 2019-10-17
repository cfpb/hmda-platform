package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.CreditScoreExempt
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V710Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V710

  property("If credit is reported exempt, all fields must be reported exempt") {
    forAll(larGen) { lar =>
      val invalidLar1 =
        lar.copy(applicant = lar.applicant.copy(creditScore = 1111))
      invalidLar1.mustFail

      val invalidLar2 = lar.copy(
        coApplicant = lar.coApplicant.copy(creditScoreType = CreditScoreExempt))
      invalidLar2.mustFail

      val validLar = lar.copy(
        applicant = lar.applicant.copy(creditScore = 1111,
                                       creditScoreType = CreditScoreExempt,
                                       otherCreditScoreModel = ""),
        coApplicant = lar.coApplicant.copy(creditScore = 1111,
                                           creditScoreType = CreditScoreExempt,
                                           otherCreditScoreModel = "")
      )
      validLar.mustPass

      val invalidLar3 = validLar.copy(
        coApplicant = validLar.coApplicant.copy(otherCreditScoreModel = "test"))
      invalidLar3.mustFail
    }
  }
}
