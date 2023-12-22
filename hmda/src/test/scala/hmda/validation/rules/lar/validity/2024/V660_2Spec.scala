package hmda.validation.rules.lar.validity._2024

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V660_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V660_2

  property("Credit score type must be valid") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.creditScoreType != CreditScoreNoCoApplicant) {
        lar.mustPass
      }

      lar
        .copy(
          applicant =
            lar.applicant.copy(creditScoreType = CreditScoreNoCoApplicant))
        .mustFail
    }
  }
}
