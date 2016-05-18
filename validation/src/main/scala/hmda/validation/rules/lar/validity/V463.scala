package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V463 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V463"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.coEthnicity is equalTo(5)) {
      (lar.applicant.coRace1 is equalTo(8)) and
        (lar.applicant.coSex is equalTo(5))
    }
  }
}
