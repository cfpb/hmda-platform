package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q617 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q617"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(
      lar.property.propertyValue not oneOf("NA", "Exempt") and
        (lar.loan.combinedLoanToValueRatio not oneOf("NA", "Exempt"))) {
      val propValue =
        Try(lar.property.propertyValue.toDouble).getOrElse(Double.MaxValue)
      val combinedLoanValueRatio =
        Try(lar.loan.combinedLoanToValueRatio.toDouble).getOrElse(0.0)
      val calculatedRatio = (lar.loan.amount / propValue) * 100
      combinedLoanValueRatio is greaterThan(calculatedRatio)
    }
  }
}
