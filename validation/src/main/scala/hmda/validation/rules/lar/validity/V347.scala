package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Result, Success }
import hmda.validation.rules.EditCheck

object V347 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V347"

  def apply(lar: LoanApplicationRegister): Result = {
    // TODO replace with when, after relevant merge happens
    if ((1 to 9).contains(lar.purchaserType)) {
      lar.actionTakenType is containedIn(List(1, 6))
    } else {
      Success()
    }
  }
}
