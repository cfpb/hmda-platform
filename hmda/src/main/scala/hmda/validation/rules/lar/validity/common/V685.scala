package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidInterestOnlyPaymentCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V685 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V685"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.nonAmortizingFeatures.interestOnlyPayments not equalTo(new InvalidInterestOnlyPaymentCode)
}
