package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck


object Q621 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q621"

  override def parent: String = "Q621"
  // valid number between 4 and 12 digits
  val regex1 = "^\\d{4,12}$"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    if (lar.larIdentifier.NMLSRIdentifier.matches(regex1) ) {
      ValidationSuccess
    } else
    {
      ValidationFailure
    }
  }
}