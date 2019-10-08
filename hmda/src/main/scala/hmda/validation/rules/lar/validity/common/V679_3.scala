package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V679_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V679-3"

  override def parent: String = "V679"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.multiFamilyAffordableUnits is numeric) {
      lar.loan.debtToIncomeRatio is oneOf("NA", "Exempt")
    }
}
