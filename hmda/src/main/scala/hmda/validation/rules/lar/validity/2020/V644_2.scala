package hmda.validation.rules.lar.validity._2020

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V644_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V644-2"

  override def parent: String = "V644"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.sex.sexEnum is equalTo(MaleAndFemale)) {
      lar.applicant.sex.sexObservedEnum is oneOf(NotVisualOrSurnameSex, SexObservedNotApplicable)
    }
}
