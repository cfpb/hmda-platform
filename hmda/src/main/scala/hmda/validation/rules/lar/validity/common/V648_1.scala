package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V648_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V648-1"

  override def parent: String = "V648"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.sex.sexObservedEnum is equalTo(NotVisualOrSurnameSex)) {
      lar.coApplicant.sex.sexEnum is oneOf(Male, Female, SexInformationNotProvided, MaleAndFemale)
    }
}
