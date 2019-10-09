package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q613 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q613"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.businessOrCommercialPurpose is equalTo(PrimarilyBusinessOrCommercialPurpose)) {
      lar.loan.loanPurpose is oneOf(HomePurchase, HomeImprovement, Refinancing, CashOutRefinancing, LoanPurposeNotApplicable)
    }
}
