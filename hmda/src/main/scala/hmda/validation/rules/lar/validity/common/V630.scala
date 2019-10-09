package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V630 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V630"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.ethnicity.ethnicity1 is equalTo(EthnicityNotApplicable)) {
      lar.applicant.ethnicity.ethnicityObserved is equalTo(EthnicityObservedNotApplicable)
    }
}
