package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.model.filing.lar.enums.Conventional

import scala.math.BigDecimal.RoundingMode
import scala.util.Try

object Q617 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q617"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.loan.loanType is equalTo(Conventional) and
        (lar.loan.combinedLoanToValueRatio not oneOf("NA", "Exempt"))
    ) {

      val propValue =
        Try(lar.property.propertyValue.toDouble).getOrElse(Double.MaxValue)
      val combinedLoanValueRatio =
        Try(BigDecimal(lar.loan.combinedLoanToValueRatio))
          .getOrElse(BigDecimal(0))

      val combinedLoanValueRatioStripped = combinedLoanValueRatio.underlying().stripTrailingZeros()

      val precision = getPrecision(combinedLoanValueRatioStripped)

      val calculatedRatio = (lar.loan.amount / propValue) * 100

      val ratioToPrecision =
        calculatedRatio.setScale(precision, RoundingMode.HALF_UP).underlying()

      combinedLoanValueRatioStripped is greaterThanOrEqual(ratioToPrecision)
    }

  private def getPrecision(number: BigDecimal): Int =
    if (number.isValidInt) {
      0
    } else {
      number.scale
    }
}
