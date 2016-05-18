package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V565 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is equalTo(6)) {
      lar.lienStatus is equalTo(4)
    }
  }

  override def name = "V565"
}