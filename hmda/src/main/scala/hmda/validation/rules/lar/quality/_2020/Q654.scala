package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import scala.util.Try

object Q654 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q654"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(Try(lar.income.toInt).getOrElse(-1) is greaterThan(5) and (lar.action.actionTakenType is oneOf(LoanOriginated, ApplicationApprovedButNotAccepted, PreapprovalRequestApprovedButNotAccepted))) {
      (lar.loan.debtToIncomeRatio  is equalTo("NA")) or
        (lar.loan.debtToIncomeRatio  is equalTo("Exempt")) or
        (lar.loan.debtToIncomeRatio is between("0","80"))    }
  }
}