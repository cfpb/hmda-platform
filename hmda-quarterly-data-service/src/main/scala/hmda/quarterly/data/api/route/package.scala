package hmda.quarterly.data.api

import hmda.quarterly.data.api.dto.QuarterGraphData._

package object route {
  val graphRoutes: GraphRouteInfo = GraphRouteInfo(
    "The following graphs present data for the 19 financial institutions" +
      " reporting HMDA quarterly data throughout 2020 and displays data for each of those institutions" +
      " for 2019 and 2018 as well.",
    Seq(ApplicationsVolume, LoansVolume, MedianCreditScores, MedianCreditScoresCCByRace, MedianCreditScoresFHAByRace,
      MedianCLTVByType, MedianCLTVCCByRace, MedianCLTVFHAByRace, MedianDTIByType, MedianDTICCByRace, MedianDTIFHAByRace,
      DenialRatesByType, DenialRatesCCByRace, DenialRatesFHAByRace))
}
