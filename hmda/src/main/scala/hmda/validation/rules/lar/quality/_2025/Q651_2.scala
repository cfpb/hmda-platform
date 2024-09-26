package hmda.validation.rules.lar.quality._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try


object Q651_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q651-2"

  override def parent: String = "Q651"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    when(lar.loan.combinedLoanToValueRatio is numeric) {
      lar.loan.combinedLoanToValueRatio.toFloat is lessThan(150)
    }
  }
}
