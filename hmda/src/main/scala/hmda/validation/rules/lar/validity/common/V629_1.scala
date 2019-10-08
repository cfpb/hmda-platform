package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ EthnicityObservedNotApplicable, NotVisualOrSurnameEthnicity, VisualOrSurnameEthnicity }
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V629_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V629-1"

  override def parent: String = "V629"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.applicant.ethnicity.ethnicityObserved is oneOf(VisualOrSurnameEthnicity,
                                                       NotVisualOrSurnameEthnicity,
                                                       EthnicityObservedNotApplicable)

}
