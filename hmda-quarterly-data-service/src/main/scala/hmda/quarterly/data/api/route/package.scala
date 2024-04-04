package hmda.quarterly.data.api

import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.counts.loans.{AllApplicationsVolume, AllApplicationsVolumeLoanPurposeHome, AllApplicationsVolumeLoanPurposeRefinance, ApplicationsVolume, ApplicationsVolumeLoanPurposeHome, ApplicationsVolumeLoanPurposeRefinance, LoansVolume, LoansVolumeLoanPurposeHome, LoansVolumeLoanPurposeRefinance}
import hmda.quarterly.data.api.route.lib.Verbiage.SUMMARY
import hmda.quarterly.data.api.route.rates.credits._
import hmda.quarterly.data.api.route.rates.denials._
import hmda.quarterly.data.api.route.rates.dti._
import hmda.quarterly.data.api.route.rates.interests._
import hmda.quarterly.data.api.route.rates.ltv._
import hmda.quarterly.data.api.route.rates.tlc._

package object route {
  val graphRoutes: GraphRouteInfo = GraphRouteInfo(SUMMARY,
    Seq(MedianCreditScore.getMedianCreditScoresSummaryRoute, MedianCreditScore.getMedianCreditScoresCCByRaceSummaryRoute,
      MedianCreditScore.getMedianCreditScoresCCByRaceLoanPurposeHomeSummaryRoute, MedianCreditScore.getMedianCreditScoresCCByRaceLoanPurposeRefinanceSummaryRoute,
      MedianCreditScore.getMedianCreditScoresFHAByRaceSummaryRoute, MedianCreditScore.getMedianCreditScoresFHAByRaceLoanPurposeHomeSummaryRoute,
      MedianCreditScore.getMedianCreditScoresFHAByRaceLoanPurposeRefinanceSummaryRoute, MedianCreditScore.getMedianCreditScoresLoanPurposeRefinanceSummaryRoute,
      MedianCreditScore.getMedianCreditScoresLoanPurposeHomeSummaryRoute, Denials.getDenialRatesByTypeSummaryRoute,
      Denials.getDenialRatesByTypeSummaryRoute, Denials.getDenialRatesByTypeLoanPurposeHomeSummaryRoute,
      Denials.getDenialRatesByTypeLoanPurposeRefinanceSummaryRoute, Denials.getDenialRatesCCByRaceSummaryRoute,
      Denials.getDenialRatesCCByRaceLoanPurposeHomeSummaryRoute, Denials.getDenialRatesCCByRaceLoanPurposeRefinanceSummaryRoute,
      Denials.getDenialRatesFHAByRaceSummaryRoute, Denials.getDenialRatesFHAByRaceLoanPurposeHomeSummaryRoute,
      Denials.getDenialRatesFHAByRaceLoanPurposeRefinanceSummaryRoute,
      MedianDTI.getMedianDTIByTypeSummaryRoute, MedianDTI.getMedianDTIByTypeLoanPurposeHomeSummaryRoute,
      MedianDTI.getMedianDTIByTypeLoanPurposeRefinanceSummaryRoute, MedianDTI.getMedianDTICCByRaceSummaryRoute,
      MedianDTI.getMedianDTICCByRaceLoanPurposeHomeSummaryRoute, MedianDTI.getMedianDTICCByRaceLoanPurposeRefinanceSummaryRoute,
      MedianDTI.getMedianDTIFHAByRaceSummaryRoute, MedianDTI.getMedianDTIFHAByRaceLoanPurposeHomeSummaryRoute,
      MedianDTI.getMedianDTIFHAByRaceLoanPurposeRefinanceSummaryRoute, MedianInterests.getMedianInterestRatesSummaryRoute,
      MedianInterests.getMedianInterestRatesCCByRaceSummaryRoute, MedianInterests.getMedianInterestRatesCCByRaceLoanPurposeHomeSummaryRoute,
      MedianInterests.getMedianInterestRatesCCByRaceLoanPurposeRefinanceSummaryRoute, MedianInterests.getMedianInterestRatesFHAByRaceSummaryRoute,
      MedianInterests.getMedianInterestRatesFHAByRaceLoanPurposeHomeSummaryRoute, MedianInterests.getMedianInterestRatesFHAByRaceLoanPurposeRefinanceSummaryRoute,
      MedianInterests.getMedianInterestRatesLoanPurposeHomeSummaryRoute, MedianInterests.getMedianInterestRatesLoanPurposeRefinanceSummaryRoute,
      MedianCLTV.getMedianCLTVByTypeSummaryRoute, MedianCLTV.getMedianCLTVByTypeLoanPurposeHomeSummaryRoute,
      MedianCLTV.getMedianCLTVByTypeLoanPurposeRefinanceSummaryRoute, MedianCLTV.getMedianCLTVCCByRaceSummaryRoute,
      MedianCLTV.getMedianCLTVCCByRaceLoanPurposeHomeSummaryRoute, MedianCLTV.getMedianCLTVCCByRaceLoanPurposeRefinanceSummaryRoute,
      MedianCLTV.getMedianCLTVFHAByRaceSummaryRoute, MedianCLTV.getMedianCLTVFHAByRaceLoanPurposeHomeSummaryRoute,
      MedianCLTV.getMedianCLTVFHAByRaceLoanPurposeRefinanceSummaryRoute, MedianTotalLoanCost.getMedianTotalLoanCostsSummaryRoute,
      MedianTotalLoanCost.getMedianTotalLoanCostsCCByRaceSummaryRoute, MedianTotalLoanCost.getMedianTotalLoanCostsCCByRaceLoanPurposeHomeSummaryRoute,
      MedianTotalLoanCost.getMedianTotalLoanCostsCCByRaceLoanPurposeRefinanceSummaryRoute, MedianTotalLoanCost.getMedianTotalLoanCostsFHAByRaceSummaryRoute,
      MedianTotalLoanCost.getMedianTotalLoanCostsFHAByRaceLoanPurposeHomeSummaryRoute, MedianTotalLoanCost.getMedianTotalLoanCostsFHAByRaceLoanPurposeRefinanceSummaryRoute,
      MedianTotalLoanCost.getMedianTotalLoanCostsLoanPurposeHomeSummaryRoute, MedianTotalLoanCost.getMedianTotalLoanCostsLoanPurposeRefinanceSummaryRoute,
      AllApplicationsVolume, AllApplicationsVolumeLoanPurposeHome,
      AllApplicationsVolumeLoanPurposeRefinance, ApplicationsVolume,
      ApplicationsVolumeLoanPurposeHome, ApplicationsVolumeLoanPurposeRefinance,
      LoansVolume, LoansVolumeLoanPurposeHome,
      LoansVolumeLoanPurposeRefinance))
}
