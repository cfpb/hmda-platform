package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V560 extends EditCheck[LoanApplicationRegister] {

  val actionTakenTypes = List(1, 2, 3, 4, 5, 7, 8)
  val lienStatusType = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is containedIn(actionTakenTypes)) {
      lar.lienStatus is containedIn(lienStatusType)
    }
  }

  def name = "V560"
}