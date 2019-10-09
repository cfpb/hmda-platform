package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V624 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V624"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.geography.zipCode is oneOf("NA", "Exempt") or
      (lar.geography.zipCode is validZipCode)
}
