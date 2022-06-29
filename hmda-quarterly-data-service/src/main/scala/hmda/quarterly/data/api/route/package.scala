package hmda.quarterly.data.api

import com.typesafe.config.ConfigFactory
import hmda.quarterly.data.api.dto.QuarterGraphData._

package object route {
  val graphRoutes: GraphRouteInfo = GraphRouteInfo(ConfigFactory.load().getString("graph.verbiage.summary"),
    Seq(ApplicationsVolume, LoansVolume, MedianCreditScores, MedianCreditScoresCCByRace, MedianCreditScoresFHAByRace,
      MedianCLTVByType, MedianCLTVCCByRace, MedianCLTVFHAByRace, MedianDTIByType, MedianDTICCByRace, MedianDTIFHAByRace,
      DenialRatesByType, DenialRatesCCByRace, DenialRatesFHAByRace, MedianInterestRates, MedianInterestRatesCCByRace, MedianInterestRatesFHAByRace,
      MedianTotalLoanCosts, MedianTotalLoanCostsCCByRace, MedianTotalLoanCostsFHAByRace))
}
