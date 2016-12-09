package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V447 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.preapprovals is equalTo(3)) {
      (lar.actionTakenType is containedIn(1 to 6))
    }
  }

  override def name = "V447"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
