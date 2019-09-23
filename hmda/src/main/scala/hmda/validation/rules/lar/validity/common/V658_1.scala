package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V658_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V658-1"

  override def parent: String = "V658"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.hoepaStatus is oneOf(HighCostMortgage, NotHighCostMortgage, HOEPStatusANotApplicable)
}
