package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V255 extends EditCheck[LoanApplicationRegister] {
  def apply(lar: LoanApplicationRegister): Result = {
    lar.actionTakenType is containedIn(1 to 8)
  }

  override def name: String = "V255"
}
