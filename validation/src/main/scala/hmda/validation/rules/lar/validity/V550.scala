package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V550 extends EditCheck[LoanApplicationRegister] {

  val lienStatusTypes = List(1, 2, 3, 4)

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.lienStatus is containedIn(lienStatusTypes)
  }

  override def name = "V550"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
