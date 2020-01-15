package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.InvalidPayableToInstitutionCode
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V694_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V694-1"

  override def parent: String = "V694"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.payableToInstitution not equalTo(new InvalidPayableToInstitutionCode)
}
