package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V225 extends EditCheck[LoanApplicationRegister] {

  val loanPurposes = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.purpose is containedIn(loanPurposes)
  }

  def name = "V225"
}
