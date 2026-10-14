package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.FHAInsured
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

trait Q624 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q624"
  protected val amount: Int

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    when(lar.loan.loanType is equalTo(FHAInsured) and (lar.property.totalUnits is equalTo(1))) {
      lar.loan.amount is lessThanOrEqual(amount)
    }
  }
}

object Q624 extends Q624 {
  private val config = ConfigFactory.load()
  override protected val amount: Int = config.getInt("edits.Q624.amount")
}
