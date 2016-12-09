package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.model.fi.lar.{ Loan, LoanApplicationRegister }
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V220 extends EditCheck[LoanApplicationRegister] {

  val loanTypes = List(1, 2, 3, 4)

  def apply(lar: LoanApplicationRegister): Result = {
    lar.loan.loanType is containedIn(loanTypes)
  }

  override def name = "V220"

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

}
