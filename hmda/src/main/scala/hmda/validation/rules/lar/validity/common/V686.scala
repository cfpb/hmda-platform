package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidNegativeArmotizationCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V686 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V686"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.nonAmortizingFeatures.negativeAmortization not equalTo(new InvalidNegativeArmotizationCode)
}
