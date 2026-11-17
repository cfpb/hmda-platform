package hmda.validation.rules.lar.quality._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.Conventional
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.lar.quality.common.Q617

import scala.math.BigDecimal.RoundingMode

object Q617 extends Q617 {
  override protected def qualifiedForEdit(lar: LoanApplicationRegister): ValidationResult =
    lar.loan.loanType is equalTo(Conventional) and
      (lar.loan.combinedLoanToValueRatio not oneOf("NA", "Exempt"))

  override protected def checkCondition(propValue: Double, combinedLoanValueRatio: BigDecimal, loanAmount: BigDecimal): ValidationResult = {
    val combinedLoanValueRatioStripped = combinedLoanValueRatio.underlying().stripTrailingZeros()

    val precision = getPrecision(combinedLoanValueRatioStripped)

    val calculatedRatio = (loanAmount / propValue) * 100

    val ratioToPrecision =
      calculatedRatio.setScale(precision, RoundingMode.HALF_UP).underlying()

    combinedLoanValueRatioStripped is greaterThanOrEqual(ratioToPrecision)
  }
}
