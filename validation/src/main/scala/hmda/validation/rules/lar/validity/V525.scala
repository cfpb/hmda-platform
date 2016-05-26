package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V525 extends EditCheck[LoanApplicationRegister] {

  val hoepaStatusTypes = List(1, 2)

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.hoepaStatus is containedIn(hoepaStatusTypes)
  }

  override def name = "V525"
}