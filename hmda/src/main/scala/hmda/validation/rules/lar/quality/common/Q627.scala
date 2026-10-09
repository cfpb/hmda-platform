package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q627 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q627"
  protected val minAmount: Int
  protected val maxAmount: Int
  protected val units: Int

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.totalUnits is greaterThanOrEqual(units)) {
      lar.loan.amount is lessThanOrEqual(maxAmount) and
        (lar.loan.amount is greaterThanOrEqual(minAmount))
    }
}

object Q627 extends Q627 {
  private val config = ConfigFactory.load()
  override protected val minAmount: Int = config.getInt("edits.Q627.minAmount")
  override protected val maxAmount: Int = config.getInt("edits.Q627.maxAmount")
  override protected val units: Int = config.getInt("edits.Q627.units")
}
