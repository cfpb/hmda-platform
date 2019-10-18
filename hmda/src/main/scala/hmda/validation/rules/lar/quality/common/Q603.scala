package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck
import hmda.model.census.Census

object Q603 {
  def withIndexedSmallCounties(indexedSmallCounties: Map[String, Census]): EditCheck[LoanApplicationRegister] =
    new Q603(indexedSmallCounties)
}

class Q603 private (indexedSmallCounties: Map[String, Census]) extends EditCheck[LoanApplicationRegister] {

  override def name: String = "Q603"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val county = lar.geography.county
    val tract  = lar.geography.tract

    if (tract.toLowerCase == "na" && county.toLowerCase != "na") {
      if (indexedSmallCounties.contains(county)) {
        ValidationSuccess
      } else {
        ValidationFailure
      }
    } else {
      ValidationSuccess
    }
  }
}
