package hmda.validation.rules.lar.quality._2019

import hmda.census.records.CensusRecords
import hmda.model.census.{ Census, State }
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck

object Q604 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "Q604"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val county = lar.geography.county
    val state  = lar.geography.state
    if (county.toLowerCase != "na" && state.toLowerCase != "na") {
      if (CensusRecords.indexedCounty2019.contains(county)) {
        val countyState = Census.states.getOrElse(state, State("", ""))
        if (county.take(2) == countyState.code) {
          ValidationSuccess
        } else {
          ValidationFailure
        }
      } else {
        ValidationFailure
      }
    } else {
      ValidationSuccess
    }
  }
}
