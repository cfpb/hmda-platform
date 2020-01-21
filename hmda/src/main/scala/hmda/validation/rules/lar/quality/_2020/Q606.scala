package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q606 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q606"

  override def parent: String = "Q606"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val income = lar.income

    when(income is numeric) {
        income.toInt is lessThan(10000)
    }
  }
}
