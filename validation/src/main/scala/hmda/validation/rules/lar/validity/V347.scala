package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V347 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  override def name: String = "V347"

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is containedIn(1 to 9)) {
      lar.actionTakenType is containedIn(List(1, 6))
    }
  }
}
