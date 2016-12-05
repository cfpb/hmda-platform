package hmda.validation.rules.lar.quality

import com.typesafe.config.ConfigFactory
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q040 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val config = ConfigFactory.load()
    val rateSpread = config.getString("hmda.validation.quality.Q040.rate-spread")

    when((lar.purchaserType is oneOf(1, 2, 3, 4)) and (lar.lienStatus is oneOf(1, 2))) {
      (lar.rateSpread is numericallyLessThanOrEqual(rateSpread)) or (lar.rateSpread is equalTo("NA"))
    }
  }

  override def name = "Q040"

  override def description = ""
}
