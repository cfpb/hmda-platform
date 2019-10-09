package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.PurchasedLoan
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V610_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V610-2"

  override def parent: String = "V610"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is equalTo(PurchasedLoan)) {
      lar.loan.applicationDate is equalTo("NA")
    } and
      when(lar.loan.applicationDate is equalTo(("NA"))) {
        lar.action.actionTakenType is equalTo(PurchasedLoan)
      }
}
