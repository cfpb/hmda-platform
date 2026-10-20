package hmda.validation.rules.lar.quality.common

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
  override protected val amount: Int = 1050000
  override protected val units: Int = 4
}
