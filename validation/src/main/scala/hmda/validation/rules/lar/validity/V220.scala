package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ LoanApplicationRegister, Loan }
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object V220 extends EditCheck[LoanApplicationRegister] {

  val loanTypes = List(1, 2, 3, 4)

  def apply(loan: Loan): Result = {
    loan.loanType is containedIn(loanTypes)
  }

  def apply(lar: LoanApplicationRegister) = this.apply(lar.loan)

  def name = "V220"
}
