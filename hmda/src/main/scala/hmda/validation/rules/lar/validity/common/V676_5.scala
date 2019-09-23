package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V676_5 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V676-5"

  override def parent: String = "V676"

  val actionList = List(
    ApplicationApprovedButNotAccepted,
    ApplicationDenied,
    ApplicationWithdrawnByApplicant,
    FileClosedForIncompleteness,
    PreapprovalRequestDenied,
    PreapprovalRequestApprovedButNotAccepted
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is containedIn(actionList)) {
      lar.loanDisclosure.lenderCredits is oneOf("NA", "Exempt")
    }
}
