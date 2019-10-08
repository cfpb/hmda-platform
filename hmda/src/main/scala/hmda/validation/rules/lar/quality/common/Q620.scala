package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.NotPrimarilyBusinessOrCommercialPurpose
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q620 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q620"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.businessOrCommercialPurpose is equalTo(NotPrimarilyBusinessOrCommercialPurpose)) {
      lar.larIdentifier.NMLSRIdentifier not equalTo("NA")
    }
}
