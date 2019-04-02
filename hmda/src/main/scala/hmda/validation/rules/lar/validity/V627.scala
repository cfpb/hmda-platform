package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V627 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V627"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(
      lar.geography.county not equalTo("NA") and (lar.geography.tract not equalTo(
        "NA"))) {
      when(
        lar.geography.county.size is greaterThanOrEqual(5) and (lar.geography.tract.size is greaterThanOrEqual(
          5))) {
        lar.geography.tract.substring(0, 5) is equalTo(lar.geography.county)
      }
    }
  }
}
