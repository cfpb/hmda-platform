package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object V262 extends EditCheck[LoanApplicationRegister] {

  def apply(lar: LoanApplicationRegister): Result = {
    val applicationDate = lar.loan.applicationDate
    when(applicationDate is equalTo("NA")) {
      lar.actionTakenType is equalTo(6)
    }
  }

  def name = "V262"
}
