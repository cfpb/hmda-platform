package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q602 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q602"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val geo = lar.geography
    when(
      (geo.zipCode not equalTo("NA")) and
        (geo.state not equalTo("NA")) and
        (geo.city not equalTo("NA"))
    ) {
      geo.street not equalTo("NA")
    }
  }
}
