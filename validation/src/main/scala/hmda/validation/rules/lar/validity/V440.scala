package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V440 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.preapprovals is equalTo(1)) {
      (lar.actionTakenType is containedIn((1 to 5) ++ (7 to 8)))
    }
  }

  override def name = "V440"
}
