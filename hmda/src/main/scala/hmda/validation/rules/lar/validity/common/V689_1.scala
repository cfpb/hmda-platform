package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidManufacturedHomeSecuredPropertyCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V689_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V689-1"

  override def parent: String = "V689"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.property.manufacturedHomeSecuredProperty not equalTo(new InvalidManufacturedHomeSecuredPropertyCode)
}
