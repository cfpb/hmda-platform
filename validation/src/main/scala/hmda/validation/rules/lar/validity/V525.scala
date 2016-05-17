package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ LoanApplicationRegister }
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object V525 extends EditCheck[LoanApplicationRegister] {

  val hoepaStatusTypes = List(1, 2)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.hoepaStatus is containedIn(hoepaStatusTypes)
  }

  def name = "V525"
}