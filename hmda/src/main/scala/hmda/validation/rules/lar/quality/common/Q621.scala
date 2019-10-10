package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q621 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q621"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when( lar.larIdentifier.NMLSRIdentifier.length is greaterThan(0)) {
      ( lar.larIdentifier.NMLSRIdentifier is alphaNumeric) and
      ( lar.larIdentifier.NMLSRIdentifier.length is lessThanOrEqual(12))
    }
  }
}
