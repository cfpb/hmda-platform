package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V540 extends EditCheck[LoanApplicationRegister] {

  val relevantActions = List(2, 3, 4, 5, 7, 8)
  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(relevantActions)) {
      (lar.hoepaStatus is equalTo(2))
    }
  }

  override def name = "V540"
}
