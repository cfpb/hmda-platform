package hmda.validation.rules.lar.validity._2020
import hmda.model.census.{Census, State}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object V716 {
  def withIndexedCounties(indexedCounties: Map[String, Census]): EditCheck[LoanApplicationRegister] =
    new V716(indexedCounties)
}

class V716 private (indexedCounties: Map[String, Census]) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V716"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val county = lar.geography.county
    val state  = lar.geography.state
    if (county.toLowerCase != "na" && state.toLowerCase != "na") {
      if (indexedCounties.contains(county)) { //TODO - this should be changed to 2020 https://github.com/cfpb/hmda-platform/issues/3492
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
