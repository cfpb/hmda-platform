package hmda.validation.rules.lar.validity._2025

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V619_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V619-2"


  override def parent: String = "V619"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val year = "2025"

    lar.action.actionTakenDate.toString.slice(0, 4) is equalTo(year)
  }

}
