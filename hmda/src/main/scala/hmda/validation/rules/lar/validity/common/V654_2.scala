package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V654_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V654-2"

  override def parent: String = "V654"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.multiFamilyAffordableUnits is numeric) {
      lar.income is equalTo("NA")
    }
}
