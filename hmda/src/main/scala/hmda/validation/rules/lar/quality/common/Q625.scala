package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.VAGuaranteed
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q625 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q625"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q625.amount")
    val units  = config.getInt("edits.Q625.units")

    when(lar.loan.loanType is equalTo(VAGuaranteed) and (lar.property.totalUnits is lessThanOrEqual(units))) {
      lar.loan.amount is lessThanOrEqual(amount)
    }
  }
}
