package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import org.slf4j.LoggerFactory

import scala.math.BigDecimal.RoundingMode
import scala.util.Try

object Q617 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q617"
  val log = LoggerFactory.getLogger("q617-logger")

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.property.propertyValue not oneOf("NA", "Exempt") and
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

   if ( lar.loan.combinedLoanToValueRatio =="25.860") {
     println("Original CLTV: " + combinedLoanValueRatio)

     println("Stripped CLTV: " + combinedLoanValueRatioStripped)
     println("Calculated Precision: " + precision)
     println("Calculated LTV: " + calculatedRatio)
     println("LTV to Precision :" + ratioToPrecision)
     val x = combinedLoanValueRatioStripped is greaterThanOrEqual(ratioToPrecision)
     println("Test if CLTV is GTEQ to LTV: " + x.toString)
   }

      combinedLoanValueRatioStripped is greaterThanOrEqual(ratioToPrecision)
    }

  private def getPrecision(number: BigDecimal): Int =
    if (number.isValidInt) {
      0
    } else {
      number.scale
    }
}
