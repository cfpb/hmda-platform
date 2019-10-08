package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ManufacturedHome, ManufacturedHomeSecuredNotApplicable }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q618 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q618"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.constructionMethod is equalTo(ManufacturedHome)) {
      lar.property.manufacturedHomeSecuredProperty not equalTo(ManufacturedHomeSecuredNotApplicable)
    }
}
