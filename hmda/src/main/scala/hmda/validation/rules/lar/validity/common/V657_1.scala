package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V657_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V657-1"

  override def parent: String = "V657"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.loan.rateSpread is numeric or
      (lar.loan.rateSpread is oneOf("NA", "Exempt"))
}
