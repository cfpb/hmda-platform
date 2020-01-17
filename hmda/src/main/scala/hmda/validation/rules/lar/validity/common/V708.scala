package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidBusinessOrCommercialBusinessCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V708 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V708"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.businessOrCommercialPurpose not equalTo(new InvalidBusinessOrCommercialBusinessCode)
}
