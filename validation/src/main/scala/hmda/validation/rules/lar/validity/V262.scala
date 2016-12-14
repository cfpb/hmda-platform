package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V262 extends EditCheck[LoanApplicationRegister] {

  override def apply(lar: LoanApplicationRegister): Result = {
    val applicationDate = lar.loan.applicationDate
    when(applicationDate is equalTo("NA")) {
      lar.actionTakenType is equalTo(6)
    }
  }

  override def name = "V262"
}
