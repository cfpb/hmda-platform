package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.Loan
import hmda.validation.dsl.{ CommonDsl, Result }

object V220 extends CommonDsl {

  val loanTypes = List(1, 2, 3, 4)

  def apply(loan: Loan): Result = {
    loan.loanType is containedIn(loanTypes)
  }
}
