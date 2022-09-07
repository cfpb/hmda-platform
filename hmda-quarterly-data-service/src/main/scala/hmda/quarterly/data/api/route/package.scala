package hmda.quarterly.data.api

import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.interests.{ MedianInterestRates, MedianInterestRatesCCByRace, MedianInterestRatesFHAByRace }
import hmda.quarterly.data.api.route.lib.Verbiage.SUMMARY

package object route {
  val graphRoutes: GraphRouteInfo = GraphRouteInfo(SUMMARY,
    Seq(ApplicationsVolume, AllApplicationsVolume, LoansVolume, MedianCreditScores, MedianCreditScoresCCByRace, MedianCreditScoresFHAByRace,
      MedianCLTVByType, MedianCLTVCCByRace, MedianCLTVFHAByRace, MedianDTIByType, MedianDTICCByRace, MedianDTIFHAByRace,
      DenialRatesByType, DenialRatesCCByRace, DenialRatesFHAByRace, MedianInterestRates, MedianInterestRatesCCByRace, MedianInterestRatesFHAByRace,
      MedianTotalLoanCosts, MedianTotalLoanCostsCCByRace, MedianTotalLoanCostsFHAByRace))
}
