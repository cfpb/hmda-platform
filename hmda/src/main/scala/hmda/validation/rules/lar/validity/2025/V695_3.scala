package hmda.validation.rules.lar.validity._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object V695_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V695-3"

  override def parent: String = "V695"

  // valid numbers between 4 and 5 digits
  val regex1 = "^\\d{4,12}$"
  // valid numbers either 4 digits or 12 digits
  val regex2 = "^(\\d{4}|\\d{12})$"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    if (lar.larIdentifier.NMLSRIdentifier.matches(regex1) ) {
      ValidationSuccess
    } else
      {
        ValidationFailure
      }
  }

}
