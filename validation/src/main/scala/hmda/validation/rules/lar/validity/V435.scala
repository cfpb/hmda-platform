package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V435 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.actionTakenType is containedIn(List(7, 8)))) {
      (lar.preapprovals is equalTo(1))
    }
  }

  override def name = "V435"
}
