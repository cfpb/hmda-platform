package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V644_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V644-1"

  override def parent: String = "V644"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.applicant.sex.sexObservedEnum is equalTo(NotVisualOrSurnameSex)) {
      lar.applicant.sex.sexEnum is oneOf(Male, Female, SexInformationNotProvided, MaleAndFemale)
    }
}
