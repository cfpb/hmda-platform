package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V340 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.purchaserType is containedIn(0 to 9)
  }

  override def name = "V340"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )
}
