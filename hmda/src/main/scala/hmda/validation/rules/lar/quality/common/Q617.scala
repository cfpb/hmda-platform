package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.{ ValidationFailure, ValidationResult }
import hmda.validation.rules.EditCheck

import scala.math.BigDecimal.RoundingMode
import scala.util.Try

trait Q617 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q617"

  protected def qualifiedForEdit(lar: LoanApplicationRegister): ValidationResult

  protected def checkCondition(propValue: Double, combinedLoanValueRatio: BigDecimal, loanAmount: BigDecimal): ValidationResult

  private def extractRelevantVals(lar: LoanApplicationRegister): (Double, BigDecimal, BigDecimal) = (
    Try(lar.property.propertyValue.toDouble).getOrElse(Double.MaxValue),
    Try(BigDecimal(lar.loan.combinedLoanToValueRatio)).getOrElse(BigDecimal(0)),
    lar.loan.amount
  )

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val (propValue, combinedLoanValueRatio, loanAmount) = extractRelevantVals(lar)
    when(qualifiedForEdit(lar)) {
      if (propValue > 0)
        checkCondition(propValue, combinedLoanValueRatio, loanAmount)
      else
        ValidationFailure
    }
  }

  protected def getPrecision(number: BigDecimal): Int =
    if (number.isValidInt) {
      0
    } else {
      number.scale
    }
}

object Q617 extends Q617 {
  override protected def qualifiedForEdit(lar: LoanApplicationRegister): ValidationResult =
    lar.property.propertyValue not oneOf("NA", "Exempt") and
      (lar.loan.combinedLoanToValueRatio not oneOf("NA", "Exempt"))

  override protected def checkCondition(propValue: Double, combinedLoanValueRatio: BigDecimal, loanAmount: BigDecimal): ValidationResult = {
    val precision = getPrecision(combinedLoanValueRatio)

    val calculatedRatio = (loanAmount / propValue) * 100

    val ratioToPrecision =
      calculatedRatio.setScale(precision, RoundingMode.HALF_UP)

    combinedLoanValueRatio is greaterThanOrEqual(ratioToPrecision)
  }
}