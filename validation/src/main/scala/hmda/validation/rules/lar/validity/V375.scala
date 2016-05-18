package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateDefaults._
import hmda.validation.dsl.PredicateSyntax._

object V375 extends EditCheck[LoanApplicationRegister] {

  val okLoanTypes = List(2, 3, 4)

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.purchaserType is equalTo(2)) { lar.loan.loanType is containedIn(okLoanTypes) }
  }

  override def name: String = "V375"
}
