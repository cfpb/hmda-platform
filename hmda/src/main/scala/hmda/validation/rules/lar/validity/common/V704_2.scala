package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V704_2 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V704-2"

  override def parent: String = "V704"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is equalTo(PurchasedLoan)) {
      lar.ausResult.ausResult1 is oneOf(AutomatedUnderwritingResultNotApplicable, AUSResultExempt)
    }
}
