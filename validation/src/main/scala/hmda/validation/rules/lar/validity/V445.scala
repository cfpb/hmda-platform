package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V445 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.preapprovals is equalTo(2)) {
      (lar.actionTakenType is containedIn(1 to 5))
    }
  }

  override def name = "V445"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
