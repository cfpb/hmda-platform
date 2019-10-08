package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ManufacturedHomeSecuredExempt, ManufacturedHomeSecuredNotApplicable }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V689_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V689-2"

  override def parent: String = "V689"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.multiFamilyAffordableUnits is numeric) {
      lar.property.manufacturedHomeSecuredProperty is
        oneOf(ManufacturedHomeSecuredExempt, ManufacturedHomeSecuredNotApplicable)
    }
}
