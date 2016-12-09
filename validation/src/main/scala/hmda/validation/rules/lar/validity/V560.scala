package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V560 extends EditCheck[LoanApplicationRegister] {

  val actionTakenTypes = List(1, 2, 3, 4, 5, 7, 8)
  val lienStatusType = List(1, 2, 3)

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(actionTakenTypes)) {
      lar.lienStatus is containedIn(lienStatusType)
    }
  }

  override def name = "V560"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
