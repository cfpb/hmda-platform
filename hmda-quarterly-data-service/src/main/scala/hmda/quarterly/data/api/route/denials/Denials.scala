package hmda.quarterly.data.api.route.denials

import com.typesafe.config.ConfigFactory
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphSeriesInfo, GraphSeriesSummary }

object Denials {
  private final val denialsVerbiageConfig = ConfigFactory.load().getConfig("graph.verbiage.denial")
  final val CATEGORY = denialsVerbiageConfig.getString("category")
  final val LABEL = denialsVerbiageConfig.getString("label")
  final val DENIAL_RATES_TITLE = denialsVerbiageConfig.getString("rates.title")
  final val DENIAL_RATES_SUBTITLE = denialsVerbiageConfig.getString("rates.subtitle")
  final val DENIAL_RATES_CC_BY_RACE_TITLE = denialsVerbiageConfig.getString("rates_cc_by_race.title")
  final val DENIAL_RATES_CC_BY_RACE_SUBTITLE = denialsVerbiageConfig.getString("rates_cc_by_race.subtitle")
  final val DENIAL_RATES_FHA_BY_RACE_TITLE = denialsVerbiageConfig.getString("rates_fha_by_race.title")
  final val DENIAL_RATES_FHA_BY_RACE_SUBTITLE = denialsVerbiageConfig.getString("rates_fha_by_race.subtitle")
}
trait Denials {
  import Denials._
  protected def getDenialsGraphSeriesInfo(title: String, subtitle: String, series: Seq[GraphSeriesSummary]): GraphSeriesInfo =
    GraphSeriesInfo(title, subtitle, series, yLabel = LABEL)
}