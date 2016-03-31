package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object V375 extends CommonDsl {

  val okLoanTypes = List(2, 3, 4)

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is equalTo(2), lar.loan.loanType is containedIn(okLoanTypes))
  }
}
