package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q623 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q623"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q623.amount")
    val income = config.getInt("edits.Q623.income")

    when(lar.income is numeric) {
      when(
        lar.property.totalUnits is lessThanOrEqual(4) and
          (lar.income.toInt is lessThanOrEqual(income))
      ) {
        lar.loan.amount is lessThan(amount)
      }
    }
  }
}
