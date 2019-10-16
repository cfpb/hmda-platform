package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.{ ValidationFailure, ValidationResult, ValidationSuccess }
import hmda.validation.rules.EditCheck
import hmda.model.census.Census

object V625_2 {
  def withIndexedTracts(indexedTracts: Map[String, Census]): EditCheck[LoanApplicationRegister] =
    new V625_2(indexedTracts)
}

class V625_2 private (indexedTracts: Map[String, Census]) extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V625-2"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val tract = lar.geography.tract

    if (tract.toLowerCase != "na") {
      if (indexedTracts.contains(tract)) {
        ValidationSuccess
      } else {
        ValidationFailure
      }
    } else {
      ValidationSuccess
    }
  }
}
