package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.census.Census
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V623 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V623"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val validValues = Census.states.keys.toList :+ "NA"
    lar.geography.state is containedIn(validValues)
  }
}
