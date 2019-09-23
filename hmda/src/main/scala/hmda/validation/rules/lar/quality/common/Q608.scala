package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.LoanOriginated
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q608 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q608"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.applicationDate is numeric and (lar.action.actionTakenType is equalTo(LoanOriginated))) {
      lar.action.actionTakenDate is greaterThan(lar.loan.applicationDate.toInt)
    }
}
