package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ InvalidActionTakenTypeCode, PurchasedLoan }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V619_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V619-3"

  override def parent: String = "V619"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.action.actionTakenType not oneOf(PurchasedLoan, new InvalidActionTakenTypeCode) and
        (lar.loan.applicationDate not equalTo("NA"))
    ) {
      val appDate = Try(lar.loan.applicationDate.toInt).getOrElse(Int.MaxValue)
      lar.action.actionTakenDate is greaterThanOrEqual(appDate)
    }

}
