package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object S011 extends EditCheck[Iterable[LoanApplicationRegister]] {

  override def apply(lars: Iterable[LoanApplicationRegister]): Result = {
    lars.size is greaterThan(0)
  }

  override def name = "S011"

}
