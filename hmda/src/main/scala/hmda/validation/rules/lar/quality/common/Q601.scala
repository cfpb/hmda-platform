package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q601 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q601"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.loan.applicationDate is numeric) {
      val minYear = lar.action.actionTakenDate.toString.slice(0, 4).toInt - 2
      val minDate = minYear.toString + lar.action.actionTakenDate.toString
        .slice(4, 8)
      lar.loan.applicationDate.toInt is greaterThanOrEqual(minDate.toInt)
    }
}
