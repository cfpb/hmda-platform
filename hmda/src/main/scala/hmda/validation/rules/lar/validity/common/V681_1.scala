package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V681_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V681-1"

  override def parent: String = "V681"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val clvr = Try(lar.loan.combinedLoanToValueRatio.toDouble).getOrElse(-1.0)

    lar.loan.combinedLoanToValueRatio is oneOf("NA", "Exempt") or
      (clvr is greaterThan(0.0))
  }
}
