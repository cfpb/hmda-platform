package hmda.validation.rules.lar.validity._2019

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V648_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V648-2"

  override def parent: String = "V648"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.coApplicant.sex.sexEnum is equalTo(MaleAndFemale)) {
      lar.coApplicant.sex.sexObservedEnum is oneOf(NotVisualOrSurnameSex, SexObservedNotApplicable)
    }
}
