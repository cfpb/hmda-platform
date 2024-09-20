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

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val NMLSRLen = lar.larIdentifier.NMLSRIdentifier.length
    val nmlsRID = lar.larIdentifier.NMLSRIdentifier

    when (nmlsRID not oneOf("Exempt", "NA")){
      (NMLSRLen is lessThanOrEqual(12)) and
        (NMLSRLen is greaterThanOrEqual(4))
    }
  }
}