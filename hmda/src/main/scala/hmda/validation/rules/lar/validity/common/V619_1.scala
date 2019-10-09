package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V619_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V619-1"

  override def parent: String = "V619"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.action.actionTakenDate.toString is validDateFormat

}
