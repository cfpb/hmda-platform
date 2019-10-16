package hmda.validation.rules.lar.quality._2018

import hmda.census.records.CensusRecords
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck

object Q604 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "Q604"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val county = lar.geography.county
    val state  = lar.geography.state
    if (county.toLowerCase != "na" && state.toLowerCase != "na") {
      if (CensusRecords.indexedCounty2018.contains(county)) {
        ValidationSuccess
      } else {
        ValidationFailure
      }
    } else {
      ValidationSuccess
    }
  }
}
