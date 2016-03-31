package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result, Success }

object V375 extends CommonDsl {

  val okLoanTypes = List(2, 3, 4)

  def apply(lar: LoanApplicationRegister): Result = {
    if (lar.purchaserType == 2) {
      lar.loan.loanType is containedIn(okLoanTypes)
    } else {
      Success()
    }
  }
}
