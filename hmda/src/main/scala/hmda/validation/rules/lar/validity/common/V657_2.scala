package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V657_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V657-2"

  override def parent: String = "V657"

  val actionList =
    List(ApplicationDenied, ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PurchasedLoan, PreapprovalRequestDenied)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is containedIn(actionList)) {
      lar.loan.rateSpread is oneOf("NA", "Exempt")
    }
}
