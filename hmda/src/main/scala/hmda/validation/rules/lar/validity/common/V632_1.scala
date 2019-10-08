package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{
  EthnicityObservedNoCoApplicant,
  EthnicityObservedNotApplicable,
  NotVisualOrSurnameEthnicity,
  VisualOrSurnameEthnicity
}
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V632_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String   = "V632-1"
  override def parent: String = "V632"
  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.coApplicant.ethnicity.ethnicityObserved is oneOf(
      VisualOrSurnameEthnicity,
      NotVisualOrSurnameEthnicity,
      EthnicityObservedNotApplicable,
      EthnicityObservedNoCoApplicant
    )
}
