package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q064 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is containedIn(List(1, 3))) {
      lar.hoepaStatus not equalTo(1)
    }
  }

  override def name = "Q064"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )
}
