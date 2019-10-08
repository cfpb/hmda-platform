package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateRegEx._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V608_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V608-2"

  override def parent: String = "V608"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.ULI.length is lessThan(23)) {
      lar.loan.ULI is alphanumeric and
        (lar.loan.ULI not empty)
    }
}
