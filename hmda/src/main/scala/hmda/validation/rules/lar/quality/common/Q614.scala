package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q614 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q614"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.age not equalTo(8888)) {
      (lar.applicant.age is greaterThanOrEqual(18)) and (lar.applicant.age is lessThanOrEqual(100))
    }
}
