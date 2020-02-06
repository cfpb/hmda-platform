package hmda.validation.rules.lar.validity._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V677_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V677-1"

  override def parent: String = "V677"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val interest = Try(lar.loan.interestRate.toDouble).getOrElse(-1.0)
    lar.loan.interestRate is oneOf("NA", "Exempt") or
      (interest is greaterThanOrEqual(0.0))
  }
}
