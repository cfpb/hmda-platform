package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }

object S011 extends CommonDsl {
  def apply(lars: Iterable[LoanApplicationRegister]): Result = {
    lars.size is greaterThan(0)
  }

}
