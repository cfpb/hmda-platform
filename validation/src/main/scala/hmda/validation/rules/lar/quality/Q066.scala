package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

object Q066 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val config = ConfigFactory.load()
    val rateSpread = config.getDouble("hmda.validation.quality.Q066.rate-spread")

    when(lar.rateSpread not equalTo("NA")) {
      lar.rateSpread.toDouble is lessThan(rateSpread)
    }
  }

  override def name = "Q066"
}
