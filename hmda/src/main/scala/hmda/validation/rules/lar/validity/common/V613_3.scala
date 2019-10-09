package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V613_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V613-3"

  override def parent: String = "V613"

  val actionsTaken = List(ApplicationDenied, ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is containedIn(actionsTaken)) {
      lar.action.preapproval is equalTo(PreapprovalNotRequested)
    }
}
