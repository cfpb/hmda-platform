package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object V230 extends EditCheck[LoanApplicationRegister] {

  val occupancyStatusTypes = List(1, 2, 3)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.occupancy is containedIn(occupancyStatusTypes)
  }

  def name = "V230"
}
