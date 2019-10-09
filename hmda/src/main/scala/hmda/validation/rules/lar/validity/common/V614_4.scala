package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ OpenEndLineOfCredit, PreapprovalNotRequested, PreapprovalRequested }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V614_4 extends EditCheck[LoanApplicationRegister] {

  val preapprovalValues = List(PreapprovalRequested, PreapprovalNotRequested)

  override def name: String = "V614-4"

  override def parent: String = "V614"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.lineOfCredit is equalTo(OpenEndLineOfCredit)) {
      lar.action.preapproval is equalTo(PreapprovalNotRequested)
    }
}
