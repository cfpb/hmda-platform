package hmda.calculator.api.validation

import hmda.calculator.api.model.RateSpreadModel.RateSpread
import hmda.calculator.entity.AporEntity

object RateSpread {

  def rateSpreadMap(rateSpread: RateSpread): RateSpread = {
    rateSpread
  }

  def validateRateSpread(rs: String): Boolean = {
    true
  }
}
