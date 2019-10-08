package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V646_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V646-2"

  override def parent: String = "V646"

  val validValues = List(VisualOrSurnameSex, NotVisualOrSurnameSex, SexObservedNotApplicable, SexObservedNoCoApplicant)

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.coApplicant.sex.sexObservedEnum is containedIn(validValues)
}
