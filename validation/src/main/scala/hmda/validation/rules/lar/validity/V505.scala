package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V505 extends EditCheck[LoanApplicationRegister] {

  val actionTakenList = List(1, 2, 3, 4, 5, 6, 7, 8)

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(actionTakenList)) {
      lar.rateSpread is equalTo("NA")
    }
  }

  override def name = "V505"
}
