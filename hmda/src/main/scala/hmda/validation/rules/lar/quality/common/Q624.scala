package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.FHAInsured
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q624 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q624"
  protected val amount: Int

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    when(lar.loan.loanType is equalTo(FHAInsured) and (lar.property.totalUnits is equalTo(1))) {
      lar.loan.amount is lessThanOrEqual(amount)
    }
  }
}

object Q624 extends Q624 {
  override protected val amount: Int = 637000
}
