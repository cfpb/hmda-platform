package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.ExemptDenialReason
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult

object V711_1 extends V711 {

  override def name: String = "V711-1"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    anyExemption(lar) {
      lar.denial.denialReason1 is equalTo(ExemptDenialReason)
    }
}
