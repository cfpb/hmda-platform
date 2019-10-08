package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ ApplicationWithdrawnByApplicant, FileClosedForIncompleteness }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V688_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V688-2"

  override def parent: String = "V688"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.action.actionTakenType is oneOf(ApplicationWithdrawnByApplicant, FileClosedForIncompleteness)) {
      lar.property.propertyValue is oneOf("Exempt", "NA")
    }
}
