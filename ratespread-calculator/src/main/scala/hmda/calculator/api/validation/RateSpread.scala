package hmda.calculator.api.validation

import hmda.calculator.api.CalculatedRateSpreadModel.CalculatedRateSpread
import hmda.calculator.api.model.RateSpreadModel.RateSpread

object RateSpread {

  def rateSpreadCalculation(rateSpread: RateSpread): CalculatedRateSpread = {

    //1.) Validate the Rate Spread Data Otherwise fail

    //2.) Calculate RateSpread and return CalculatedRateSpread if validation passes

    //{"rateSpread":"5.540"}
    val fakeRate = "5.540"
    val calculatedRateSpread = CalculatedRateSpread(fakeRate)
    calculatedRateSpread
  }

  def validateRateSpread(rs: String): Boolean = {
    true
  }
}
