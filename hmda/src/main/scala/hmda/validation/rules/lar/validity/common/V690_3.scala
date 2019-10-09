package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ManufacturedHomeLandNotApplicable, ManufacturedHomeLoanPropertyInterestExempt, SiteBuilt }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V690_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V690-3"

  override def parent: String = "V690"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.constructionMethod is equalTo(SiteBuilt)) {
      lar.property.manufacturedHomeLandPropertyInterest is
        oneOf(ManufacturedHomeLandNotApplicable, ManufacturedHomeLoanPropertyInterestExempt)
    }
}
