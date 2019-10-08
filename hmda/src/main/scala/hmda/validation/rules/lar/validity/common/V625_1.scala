package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V625_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V625-1"

  override def parent: String = "V625"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    (lar.geography.tract is numeric and (lar.geography.tract.size is equalTo(11))) or (lar.geography.tract.toLowerCase is equalTo("na"))
}
