package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object V340 extends CommonDsl {

  def apply(lar: LoanApplicationRegister): Result = {
    lar.purchaserType is containedIn(0 to 9)
  }
}
