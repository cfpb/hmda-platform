package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.HOEPStatusANotApplicable
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q630 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q630"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(lar.property.totalUnits is greaterThanOrEqual(5)) {
      lar.hoepaStatus is equalTo(HOEPStatusANotApplicable)
    }
  }
}
