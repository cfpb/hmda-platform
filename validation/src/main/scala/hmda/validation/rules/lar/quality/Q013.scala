package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object Q013 extends EditCheck[LoanApplicationRegister] {

  val config = ConfigFactory.load()
  val minAmount = config.getInt("hmda.validation.quality.Q013.loan.min-amount")
  val maxAmount = config.getInt("hmda.validation.quality.Q013.loan.max-amount")

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.loan.propertyType is equalTo(3)) {
      lar.loan.amount is between(minAmount, maxAmount)
    }
  }

  override def name = "Q013"
}
