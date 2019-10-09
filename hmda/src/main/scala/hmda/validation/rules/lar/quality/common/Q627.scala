package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q627 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q627"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config    = ConfigFactory.load()
    val minAmount = config.getInt("edits.Q627.minAmount")
    val maxAmount = config.getInt("edits.Q627.maxAmount")
    val units     = config.getInt("edits.Q627.units")

    when(lar.property.totalUnits is greaterThanOrEqual(units)) {
      lar.loan.amount is lessThanOrEqual(maxAmount) and
        (lar.loan.amount is greaterThanOrEqual(minAmount))
    }
  }
}
