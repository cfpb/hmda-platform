package hmda.validation.rules.lar.validity

import hmda.census.records.CensusRecords
import hmda.model.filing.lar._2018.LoanApplicationRegister
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object V625_2 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "V625-2"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val tract = lar.geography.tract

    if (tract.toLowerCase != "na") {
      if (CensusRecords.indexedTract.contains(tract)) {
        ValidationSuccess
      } else {
        ValidationFailure
      }
    } else {
      ValidationSuccess
    }
  }
}
