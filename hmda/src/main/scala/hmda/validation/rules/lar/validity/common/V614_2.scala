package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.PreapprovalNotRequested
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V614_2 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V614-2"

  override def parent: String = "V614"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.property.multiFamilyAffordableUnits is numeric) {
      lar.action.preapproval is equalTo(PreapprovalNotRequested)
    }
}
