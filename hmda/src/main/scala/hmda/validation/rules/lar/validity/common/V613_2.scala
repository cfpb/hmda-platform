package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ PreapprovalRequestApprovedButNotAccepted, PreapprovalRequestDenied, PreapprovalRequested }
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V613_2 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V613-2"

  override def parent: String = "V613"

  val actionsTaken =
    List(PreapprovalRequestDenied, PreapprovalRequestApprovedButNotAccepted)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is containedIn(actionsTaken)) {
      lar.action.preapproval is equalTo(PreapprovalRequested)
    }
}
