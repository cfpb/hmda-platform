package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ AUSExempt, AUSNotApplicable, PurchasedLoan }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V704_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V704-1"

  override def parent: String = "704"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is equalTo(PurchasedLoan)) {
      lar.AUS.aus1 is oneOf(AUSNotApplicable, AUSExempt)
    }
}
