package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V450 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V450"

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.ethnicity is containedIn(1 to 4)
  }

}
