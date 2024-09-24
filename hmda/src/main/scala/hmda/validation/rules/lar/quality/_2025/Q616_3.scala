package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.filing.ts.TsCsvParser.toValidBigDecimal
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateHmda._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

import scala.util.Try


object Q616_3 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q616-3"

  override def parent: String = "Q616"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val discountPoints = toValidBigDecimal(lar.loanDisclosure.discountPoints)
    val fifteenPercentLoanAmount = (lar.loan.amount * 0.15).setScale(2, BigDecimal.RoundingMode.HALF_UP)

    discountPoints is  lessThanOrEqual(fifteenPercentLoanAmount)
  }
}
