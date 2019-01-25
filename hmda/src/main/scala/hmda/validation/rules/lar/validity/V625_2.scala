package hmda.validation.rules.lar.validity

import hmda.HmdaPlatform
import hmda.census.records.CensusRecords
import hmda.census.validation.CensusValidation
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{
  ValidationFailure,
  ValidationResult,
  ValidationSuccess
}
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
