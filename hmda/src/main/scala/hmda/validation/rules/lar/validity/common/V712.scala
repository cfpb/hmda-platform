package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V712 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V712"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(
      lar.loanDisclosure.totalPointsAndFees is equalTo("Exempt") or
        (lar.loanDisclosure.totalLoanCosts is equalTo("Exempt"))
    ) {
      lar.loanDisclosure.totalPointsAndFees is equalTo("Exempt") and
        (lar.loanDisclosure.totalLoanCosts is equalTo("Exempt"))
    }
}
