package hmda.validation.rules.lar.validity._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object V695_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V695-3"

  override def parent: String = "V695"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val NMLSRLen = lar.larIdentifier.NMLSRIdentifier.length
    val nmlsRID = lar.larIdentifier.NMLSRIdentifier

    (NMLSRLen is lessThanOrEqual(12)) and
      (NMLSRLen is greaterThanOrEqual(4)) and
      ( nmlsRID is numeric)
  }

}
