package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V634 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V634"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.ethnicity.ethnicity1 is equalTo(EthnicityNoCoApplicant)) {
      lar.coApplicant.ethnicity.ethnicityObserved is equalTo(EthnicityObservedNoCoApplicant)
    } and when(lar.coApplicant.ethnicity.ethnicityObserved is equalTo(EthnicityObservedNoCoApplicant)) {
      lar.coApplicant.ethnicity.ethnicity1 is equalTo(EthnicityNoCoApplicant)
    }
}
