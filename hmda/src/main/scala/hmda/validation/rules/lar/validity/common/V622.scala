package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V622 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V622"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.geography.street not oneOf("NA", "Exempt")) {
      lar.geography.city not equalTo("NA") and
        (lar.geography.state not equalTo("NA")) and
        (lar.geography.zipCode not equalTo("NA"))
    }
}
