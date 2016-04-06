package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object S011 extends EditCheck[Iterable[LoanApplicationRegister]] {
  def apply(lars: Iterable[LoanApplicationRegister]): Result = {
    lars.size is greaterThan(0)
  }

  def name = "S011"

}
