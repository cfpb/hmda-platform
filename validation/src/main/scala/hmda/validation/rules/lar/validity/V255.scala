package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Result, CommonDsl }

object V255 extends CommonDsl {
  def apply(lar: LoanApplicationRegister): Result = {
    lar.actionTakenType is containedIn(1 to 8)
  }
}
