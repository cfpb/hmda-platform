package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ PreapprovalNotRequested, PreapprovalRequested, ReverseMortgage }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V614_3 extends EditCheck[LoanApplicationRegister] {

  val preapprovalValues = List(PreapprovalRequested, PreapprovalNotRequested)

  override def name: String = "V614-3"

  override def parent: String = "V614"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.reverseMortgage is equalTo(ReverseMortgage)) {
      lar.action.preapproval is equalTo(PreapprovalNotRequested)
    }
}
