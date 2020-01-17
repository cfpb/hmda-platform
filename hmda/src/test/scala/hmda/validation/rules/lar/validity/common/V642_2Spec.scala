package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.LarEditCheckSpec

class V642_2Spec extends LarEditCheckSpec {
  override def check: EditCheck[LoanApplicationRegister] = V642_2

  property("Applicant sex observed value must be valid") {
    forAll(larGen) { lar =>
      whenever(lar.applicant.sex.sexObservedEnum != SexObservedNoCoApplicant) {
        lar.mustPass
        val invalidLar = lar.copy(
          applicant = lar.applicant.copy(sex =
            lar.applicant.sex.copy(sexObservedEnum = new InvalidSexObservedCode)))
        invalidLar.mustFail
      }
    }
  }
}
