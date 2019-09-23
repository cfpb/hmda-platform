package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V613_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V613-4"

  override def parent: String = "V613"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.preapproval is equalTo(PreapprovalRequested)) {
      (lar.action.actionTakenType is equalTo(LoanOriginated)) or
        (lar.action.actionTakenType is equalTo(ApplicationApprovedButNotAccepted)) or
        (lar.action.actionTakenType is equalTo(PreapprovalRequestDenied)) or
        (lar.action.actionTakenType is equalTo(PreapprovalRequestApprovedButNotAccepted))
    }

}
