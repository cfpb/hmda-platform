package hmda.validation.rules.lar.validity

import hmda.model.census.Census
import hmda.model.filing.lar._2018.LoanApplicationRegister
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
