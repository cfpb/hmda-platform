package hmda.validation.rules.lar.quality._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q629 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q629"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.action.actionTakenType.code is oneOf(1, 2, 3, 4, 5, 7, 8) and
        (lar.property.totalUnits is lessThanOrEqual(4)) and
        (lar.loan.loanPurpose.code is oneOf(1, 2, 4)) and
        (lar.businessOrCommercialPurpose.code is equalTo(2))
    ) {
      lar.income not equalTo("NA")
    }
}
