package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums._
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q605_2 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q605-2"

  override def parent: String = "Q605"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.purchaserType is equalTo(GinnieMae)) {
      lar.loan.loanType is oneOf(FHAInsured, VAGuaranteed, RHSOrFSAGuaranteed)
    }
}
