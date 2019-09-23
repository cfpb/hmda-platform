package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V670_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V670-4"

  override def parent: String = "V670"

  val actionList = List(
    LoanOriginated,
    ApplicationApprovedButNotAccepted,
    ApplicationWithdrawnByApplicant,
    FileClosedForIncompleteness,
    PurchasedLoan,
    PreapprovalRequestApprovedButNotAccepted
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.denial.denialReason1 is equalTo(DenialReasonNotApplicable)) {
      lar.action.actionTakenType is containedIn(actionList)
    }
}
