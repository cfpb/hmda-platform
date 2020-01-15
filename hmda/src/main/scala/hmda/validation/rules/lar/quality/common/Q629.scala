package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q629 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q629"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.action.actionTakenType not oneOf(PurchasedLoan, new InvalidActionTakenTypeCode) and
        (lar.property.totalUnits is lessThanOrEqual(4)) and
        (lar.loan.loanPurpose is oneOf(HomePurchase, HomeImprovement, OtherPurpose))
    ) {
      lar.income not equalTo("NA")
    }
}
