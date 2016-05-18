package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object V347 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V347"

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is containedIn(1 to 9)) {
      lar.actionTakenType is containedIn(List(1, 6))
    }
  }
}
