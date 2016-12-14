package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q032 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.actionTakenType is equalTo(1)) {
      lar.actionTakenDate.toString not equalTo(lar.loan.applicationDate)
    }
  }

  override def name = "Q032"
}
