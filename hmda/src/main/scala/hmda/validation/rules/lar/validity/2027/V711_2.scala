package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.EmptyDenialValue
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult

object V711_2 extends V711 {

  override def name: String = "V711-2"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    anyExemption(lar) {
      lar.denial.denialReason2 is equalTo(EmptyDenialValue) and
        (lar.denial.denialReason3 is equalTo(EmptyDenialValue)) and
        (lar.denial.denialReason4 is equalTo(EmptyDenialValue)) and
        (lar.denial.otherDenialReason is empty)
    }
}