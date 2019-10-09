package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V642_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V642-2"

  override def parent: String = "V642"

  val validSexValues =
    List(VisualOrSurnameSex, NotVisualOrSurnameSex, SexObservedNotApplicable)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.applicant.sex.sexObservedEnum is containedIn(validSexValues)
}
