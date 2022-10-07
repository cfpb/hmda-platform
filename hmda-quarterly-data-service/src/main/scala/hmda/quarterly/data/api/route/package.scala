package hmda.quarterly.data.api

import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.counts.loans.{ AllApplicationsVolume, ApplicationsVolume, LoansVolume }
import hmda.quarterly.data.api.route.lib.Verbiage.SUMMARY
import hmda.quarterly.data.api.route.rates.credits._
import hmda.quarterly.data.api.route.rates.denials._
import hmda.quarterly.data.api.route.rates.dti._
import hmda.quarterly.data.api.route.rates.interests._
import hmda.quarterly.data.api.route.rates.ltv._
import hmda.quarterly.data.api.route.rates.tlc._

package object route {
  val graphRoutes: GraphRouteInfo = GraphRouteInfo(SUMMARY,
    Seq(ApplicationsVolume, AllApplicationsVolume, LoansVolume, MedianCreditScores.getRoute, MedianCreditScoresCCByRace.getRoute, MedianCreditScoresFHAByRace.getRoute,
      MedianCLTVByType.getRoute, MedianCLTVCCByRace.getRoute, MedianCLTVFHAByRace.getRoute, MedianDTIByType.getRoute, MedianDTICCByRace.getRoute, MedianDTIFHAByRace.getRoute,
      DenialRatesByType.getRoute, DenialRatesCCByRace.getRoute, DenialRatesFHAByRace.getRoute, MedianInterestRates.getRoute, MedianInterestRatesCCByRace.getRoute, MedianInterestRatesFHAByRace.getRoute,
      MedianTotalLoanCosts.getRoute, MedianTotalLoanCostsCCByRace.getRoute, MedianTotalLoanCostsFHAByRace.getRoute))
}
