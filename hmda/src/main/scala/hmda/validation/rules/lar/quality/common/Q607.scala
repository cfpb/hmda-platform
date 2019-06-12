package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.SecuredBySubordinateLien
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q607 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q607"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val config = ConfigFactory.load()
    val loanAmount =
      config.getDouble("edits.Q607.amount")

    when(lar.lienStatus is equalTo(SecuredBySubordinateLien)) {
      lar.loan.amount is lessThanOrEqual(loanAmount)
    }
  }
}
