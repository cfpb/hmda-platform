package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V679_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V679-1"

  override def parent: String = "V679"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val dir = Try(lar.loan.debtToIncomeRatio.toDouble).isSuccess
    lar.loan.debtToIncomeRatio is oneOf("NA", "Exempt") or
      (dir is equalTo(true))
  }
}
