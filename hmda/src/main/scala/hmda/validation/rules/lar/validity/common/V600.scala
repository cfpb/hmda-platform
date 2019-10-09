package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V600 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V600"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.larIdentifier.LEI.length is equalTo(20)
}
