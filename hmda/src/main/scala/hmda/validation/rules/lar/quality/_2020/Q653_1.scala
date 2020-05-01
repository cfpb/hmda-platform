package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q653_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q653-1"

  override def parent: String = "Q653"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.action.actionTakenType is oneOf(LoanOriginated, ApplicationApprovedButNotAccepted, PreapprovalRequestApprovedButNotAccepted) and (lar.loan.combinedLoanToValueRatio not oneOf("NA", "Exempt"))) {
        lar.loan.combinedLoanToValueRatio is between("0","250")
    }
  }
}
