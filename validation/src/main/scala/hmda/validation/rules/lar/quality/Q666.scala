package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

object Q666 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q666"

  override def apply(lar: LoanApplicationRegister): Result = {
    val loanId = lar.loan.id
    loanId not allCharacters
  }

}
