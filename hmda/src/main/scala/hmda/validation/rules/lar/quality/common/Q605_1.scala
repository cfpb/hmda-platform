package hmda.validation.rules.lar.quality.common

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ Conventional, FannieMae, FreddieMac }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q605_1 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q605-1"

  override def parent: String = "Q605"

  override def apply(lar: LoanApplicationRegister): ValidationResult =
    when(lar.purchaserType is oneOf(FannieMae, FreddieMac)) {
      lar.loan.loanType is equalTo(Conventional)
    }
}
