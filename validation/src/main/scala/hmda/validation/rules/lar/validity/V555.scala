package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V555 extends EditCheck[LoanApplicationRegister] {

  val loanPurposeTypes = List(1, 3)
  val lienStatusTypes = List(1, 2, 4)

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.loan.purpose is containedIn(loanPurposeTypes)) {
      lar.lienStatus is containedIn(lienStatusTypes)
    }
  }

  override def name = "V555"
}