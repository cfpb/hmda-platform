package hmda.validation.rules.lar.quality

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.SecuredBySubordinateLien
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q607 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q607"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    when(lar.lienStatus is equalTo(SecuredBySubordinateLien)){
      lar.loan.amount is lessThan(250000)
    }
  }
}
