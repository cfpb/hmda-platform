package hmda.validation.rules.lar.validity

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.PrimarilyBusinessOrCommercialPurpose
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V672_5 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V672-5"

  override def parent: String = "V672"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.businessOrCommercialPurpose is equalTo(PrimarilyBusinessOrCommercialPurpose)) {
      lar.loanDisclosure.totalLoanCosts is oneOf("NA", "Exempt")
    }
}
