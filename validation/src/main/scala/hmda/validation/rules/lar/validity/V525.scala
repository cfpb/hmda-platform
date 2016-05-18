package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object V525 extends EditCheck[LoanApplicationRegister] {

  val hoepaStatusTypes = List(1, 2)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.hoepaStatus is containedIn(hoepaStatusTypes)
  }

  def name = "V525"
}