package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V615_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V615-1"

  override def parent: String = "V615"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.loan.constructionMethod is oneOf(SiteBuilt, ManufacturedHome)

}
