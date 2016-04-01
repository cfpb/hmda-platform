package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object V225 extends CommonDsl {

  val loanPurposes = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.purpose is containedIn(loanPurposes)
  }

}
