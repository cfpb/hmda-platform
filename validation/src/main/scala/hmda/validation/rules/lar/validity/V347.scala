package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V347 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V347"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is containedIn(1 to 9)) {
      lar.actionTakenType is oneOf(1, 6)
    }
  }
}
