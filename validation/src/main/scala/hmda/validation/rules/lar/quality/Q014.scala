package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q014 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q014"

  override def apply(lar: LoanApplicationRegister): Result = {
    val income = lar.applicant.income
    when(income is numeric) {
      income.toInt is lessThan(3000)
    }
  }
}
