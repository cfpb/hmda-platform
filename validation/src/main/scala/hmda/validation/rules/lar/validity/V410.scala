package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V410 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.lienStatus is equalTo(3)) {
      lar.loan.purpose is equalTo(2)
    }
  }

  override def name: String = "V410"

}
