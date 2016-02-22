package hmda.validation.rules.syntactical.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Success, CommonDsl, Result }

class S011 extends CommonDsl {
  def apply(lars: Iterable[LoanApplicationRegister]): Result = {
    lars.size is greaterThan(0)
  }

}
