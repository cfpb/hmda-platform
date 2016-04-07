package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V310 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    lar.applicant.race1 is containedIn(1 to 7)
  }

  override def name: String = "V310"

}
