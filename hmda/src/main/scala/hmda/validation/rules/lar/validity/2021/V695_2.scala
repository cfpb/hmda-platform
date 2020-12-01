package hmda.validation.rules.lar.validity._2021

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V695_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V695-2"

  override def parent: String = "V695"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    lar.larIdentifier.NMLSRIdentifier not equalTo("0")
  }

}
