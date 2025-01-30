package hmda.validation.rules.lar.validity.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V619_2 {
  def withYear(year: String): EditCheck[LoanApplicationRegister] =
    new V619_2(year)

}

class V619_2 private (year: String) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V619-2"

  override def parent: String = "V619"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    lar.action.actionTakenDate.toString.slice(0, 4) is equalTo(year)
  }

}