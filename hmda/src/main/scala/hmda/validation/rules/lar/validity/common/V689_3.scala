package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ManufacturedHomeSecuredExempt, ManufacturedHomeSecuredNotApplicable, SiteBuilt }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V689_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V689-3"

  override def parent: String = "V689"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.constructionMethod is equalTo(SiteBuilt)) {
      lar.property.manufacturedHomeSecuredProperty is
        oneOf(ManufacturedHomeSecuredExempt, ManufacturedHomeSecuredNotApplicable)
    }
}
