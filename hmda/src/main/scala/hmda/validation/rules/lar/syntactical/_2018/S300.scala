package hmda.validation.rules.lar.syntactical._2018

import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object S300 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "S300"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    lar.larIdentifier.id is equalTo(2)
  }

}
