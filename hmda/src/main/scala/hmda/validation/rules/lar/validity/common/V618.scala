package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V618 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V618"

  val validActionTaken = List(
    LoanOriginated,
    ApplicationApprovedButNotAccepted,
    ApplicationDenied,
    ApplicationWithdrawnByApplicant,
    FileClosedForIncompleteness,
    PurchasedLoan,
    PreapprovalRequestDenied,
    PreapprovalRequestApprovedButNotAccepted
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.action.actionTakenType is containedIn(validActionTaken)
}
