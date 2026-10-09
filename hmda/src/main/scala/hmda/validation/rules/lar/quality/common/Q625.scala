package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.VAGuaranteed
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q625 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q625"
  protected val amount: Int
  protected val units: Int

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.loanType is equalTo(VAGuaranteed) and (lar.property.totalUnits is lessThanOrEqual(units))) {
      lar.loan.amount is lessThanOrEqual(amount)
    }
}

object Q625 extends Q625 {
  private val config = ConfigFactory.load()
  override protected val amount: Int = config.getInt("edits.Q625.amount")
  override protected val units: Int = config.getInt("edits.Q625.units")
}
