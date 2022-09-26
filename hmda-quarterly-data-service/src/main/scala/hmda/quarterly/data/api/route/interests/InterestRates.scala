package hmda.quarterly.data.api.route.interests

import com.typesafe.config.ConfigFactory
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphSeriesInfo, GraphSeriesSummary }
import hmda.quarterly.data.api.route.lib.Verbiage.INTEREST_DECIMAL_PRECISION

object InterestRates {
  private final val interestsVerbiageConfig = ConfigFactory.load().getConfig("graph.verbiage.interest")
  final val CATEGORY = interestsVerbiageConfig.getString("category")
  final val LABEL = interestsVerbiageConfig.getString("label")
  final val MEDIAN_TITLE = interestsVerbiageConfig.getString("median_rates.title")
  final val MEDIAN_SUBTITLE = interestsVerbiageConfig.getString("median_rates.subtitle")
  final val MEDIAN_CC_TITLE = interestsVerbiageConfig.getString("median_cc_by_race.title")
  final val MEDIAN_CC_SUBTITLE = interestsVerbiageConfig.getString("median_cc_by_race.subtitle")
  final val MEDIAN_FHA_TITLE = interestsVerbiageConfig.getString("median_fha_by_race.title")
  final val MEDIAN_FHA_SUBTITLE = interestsVerbiageConfig.getString("median_fha_by_race.subtitle")
}
trait InterestRates {
  import InterestRates._

  protected def getInterestRatesGraphSeriesInfo(title: String, subtitle: String, series: Seq[GraphSeriesSummary]): GraphSeriesInfo =
    GraphSeriesInfo(title, subtitle, series, yLabel = LABEL, decimalPrecision = INTEREST_DECIMAL_PRECISION)
}