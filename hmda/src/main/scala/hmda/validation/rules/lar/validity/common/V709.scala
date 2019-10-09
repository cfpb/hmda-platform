package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V709 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V709"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.geography.street is equalTo("Exempt") or
        (lar.geography.city is equalTo("Exempt")) or
        (lar.geography.zipCode is equalTo("Exempt"))
    ) {

      lar.geography.street is equalTo("Exempt") and
        (lar.geography.city is equalTo("Exempt")) and
        (lar.geography.zipCode is equalTo("Exempt"))
    }
}
