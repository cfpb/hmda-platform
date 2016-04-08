package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V455 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.ethnicity is containedIn(List(1, 2, 3))) {
      lar.applicant.race1 not equalTo(7)
    }
  }

  override def name: String = "V455"

}
