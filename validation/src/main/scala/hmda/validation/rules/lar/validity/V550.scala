package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ LoanApplicationRegister }
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object V550 extends EditCheck[LoanApplicationRegister] {

  val lienStatusTypes = List(1, 2, 3, 4)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.lienStatus is containedIn(lienStatusTypes)
  }

  def name = "V550"
}