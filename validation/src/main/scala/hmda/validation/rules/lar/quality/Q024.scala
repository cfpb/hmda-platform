package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q024 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q024"

  override def apply(lar: LoanApplicationRegister): Result = {
    val income = lar.applicant.income

    when((lar.actionTakenType is equalTo(1)) and (income is numeric)) {
      when(lar.loan.amount is greaterThanOrEqual(income.toInt * 5)) {
        income.toInt is greaterThan(9)
      }
    }
  }

}
