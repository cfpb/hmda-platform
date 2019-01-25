package hmda.validation.rules.lar.quality

import hmda.HmdaPlatform
import hmda.census.validation.CensusValidation
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ValidationFailure, ValidationResult, ValidationSuccess}
import hmda.validation.rules.EditCheck

object Q603 extends EditCheck[LoanApplicationRegister] {

  override def name: String = "Q603"


  override def apply(
      lar: LoanApplicationRegister): ValidationResult = {

    val county = lar.geography.county
    val tract = lar.geography.tract

    if (tract.toLowerCase == "na" && county.toLowerCase != "na") {
      if (CensusValidation.isCountySmall(county, HmdaPlatform.indexedSmallCounty)) {
        ValidationSuccess
      }
      else {
        ValidationFailure
      }
    } else {
      ValidationSuccess
    }
  }
}
