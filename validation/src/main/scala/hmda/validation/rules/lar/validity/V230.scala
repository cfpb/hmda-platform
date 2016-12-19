package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.{ LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V230 extends EditCheck[LoanApplicationRegister] {

  val occupancyStatuses = List(1, 2, 3)

  override def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.occupancy is containedIn(occupancyStatuses)
  }

  override def name = "V230"
}
