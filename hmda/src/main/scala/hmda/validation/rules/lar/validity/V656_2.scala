package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V656_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V656-2"

  override def parent: String = "V656"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val relevantActionTaken = List(
      ApplicationApprovedButNotAccepted,
      ApplicationDenied,
      ApplicationWithdrawnByApplicant,
      FileClosedForIncompleteness,
      PreapprovalRequestDenied,
      PreapprovalRequestApprovedButNotAccepted
    )

    when(lar.action.actionTakenType is containedIn(relevantActionTaken)) {
      lar.purchaserType is equalTo(PurchaserTypeNotApplicable)
    }
  }
}
