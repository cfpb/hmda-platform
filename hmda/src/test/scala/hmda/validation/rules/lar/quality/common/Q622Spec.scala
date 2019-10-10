package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ReverseMortgage
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class Q622Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = Q622

  property("Applicant age should be applicable to a reverse mortgage") {
    forAll(larGen) { lar =>
      whenever(lar.reverseMortgage != ReverseMortgage) {
        lar.mustPass
      }

      val appLar = lar.copy(reverseMortgage = ReverseMortgage)
      appLar.copy(applicant = appLar.applicant.copy(age = 61)).mustFail
      appLar.copy(applicant = appLar.applicant.copy(age = 62)).mustPass
      appLar.copy(applicant = appLar.applicant.copy(age = 63)).mustPass
    }
  }
}
