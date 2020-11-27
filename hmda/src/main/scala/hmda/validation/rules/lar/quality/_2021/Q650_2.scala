package hmda.validation.rules.lar.quality._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon.{greaterThan, greaterThanOrEqual, when}
import hmda.validation.dsl.PredicateSyntax.PredicateOps
import hmda.validation.dsl.{ValidationFailure, ValidationResult}
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q650_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q650-2"

  override def parent: String = "Q650"

  override def apply(lar: LoanApplicationRegister): ValidationResult ={
    val interest = Try(lar.loan.interestRate.toDouble).getOrElse(-1.0)
    when(interest is greaterThan(0.0)) {
      interest is greaterThanOrEqual(20.0)
    }
  }
}
