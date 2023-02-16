package hmda.quarterly.data.api.route.rates

import hmda.quarterly.data.api.route.lib.Verbiage.INTEREST_DECIMAL_PRECISION
import hmda.quarterly.data.api.route.rates.RatesGraph.Category

abstract class InterestRatesGraph(
  config: String,
  endpoint: String,
  titleKey: String,
  subtitleKey: String,
  category: Category) extends RatesGraph(config, endpoint, titleKey, subtitleKey, category) {
  override protected def decimalPlaces: Int = INTEREST_DECIMAL_PRECISION
}
