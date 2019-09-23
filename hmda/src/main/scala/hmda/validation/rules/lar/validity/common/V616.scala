package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V616 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V616"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    lar.loan.occupancy is oneOf(PrincipalResidence, SecondResidence, InvestmentProperty)
}
