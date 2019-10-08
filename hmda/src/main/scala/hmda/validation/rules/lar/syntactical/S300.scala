package hmda.validation.rules.lar.syntactical

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object S300 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "S300"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.larIdentifier.id is equalTo(2)

}
