package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q004 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when((lar.loan.loanType is equalTo(3) and (lar.loan.propertyType is containedIn(List(1, 2))))) {
      lar.loan.amount is lessThanOrEqual(1050)
    }
  }

  override def name = "Q004"
}
