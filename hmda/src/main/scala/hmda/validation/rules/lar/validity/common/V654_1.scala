package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object V654_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V654-1"

  override def parent: String = "V654"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    ((lar.income is numeric) and (Try(lar.income.toInt).isSuccess is equalTo(true))) or
      (lar.income is equalTo("NA"))
}
