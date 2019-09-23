package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.HomePurchase
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object Q628 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q628"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val amount = config.getInt("edits.Q628.amount")

    when(lar.loan.loanPurpose is equalTo(HomePurchase) and (lar.property.totalUnits is lessThanOrEqual(4))) {
      lar.loan.amount is greaterThan(amount)
    }
  }
}
