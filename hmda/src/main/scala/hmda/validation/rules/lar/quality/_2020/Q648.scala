package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object Q648 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q648"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
      if (lar.loan.ULI.length <= 22)
        ValidationSuccess
      else
        when(lar.action.actionTakenType is oneOf(LoanOriginated, ApplicationApprovedButNotAccepted, ApplicationDenied, ApplicationWithdrawnByApplicant, FileClosedForIncompleteness, PreapprovalRequestDenied, PreapprovalRequestApprovedButNotAccepted)) {
          lar.larIdentifier.LEI.take(20) is equalTo(lar.loan.ULI.take(20))
        }
  }
}
