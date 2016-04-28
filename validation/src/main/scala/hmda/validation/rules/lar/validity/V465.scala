package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V465 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.applicant.coEthnicity is containedIn(List(1, 2, 3))) {
      lar.applicant.coRace1 not containedIn(List(7, 8))
    }
  }

  override def name: String = "V465"

}
