package hmda.validation.rules.lar.quality.common

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.lar.enums.{ FannieMae, FarmerMac, FreddieMac, GinnieMae }
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

import scala.util.Try

object Q609 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "Q609"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val config = ConfigFactory.load()
    val rs     = config.getDouble("edits.Q609.rateSpread")

    when(lar.purchaserType is oneOf(FannieMae, GinnieMae, FreddieMac, FarmerMac)) {
      val rsAsDouble = Try(lar.loan.rateSpread.toDouble).getOrElse(9999.0)

      lar.loan.rateSpread is equalTo("Exempt") or
        (lar.loan.rateSpread is equalTo("NA")) or
        (rsAsDouble is lessThanOrEqual(rs))
    }
  }
}
