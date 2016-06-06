package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q037 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.lienStatus is equalTo(2)) {
      lar.loan.amount is lessThanOrEqual(250)
    }
  }

  override def name = "Q037"
}
