package hmda.quarterly.data.api.route.rates.interests

import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.route.rates.InterestRatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.Scheduler.Implicits.global
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.{Conventional, FHAInsured, RHSOrFSAGuaranteed, VAGuaranteed}
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute}
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType.{CONVENTIONAL_CONFORMING, CONVENTIONAL_NON_CONFORMING, FHA, HELOC, RHS_FSA, VA}
import hmda.quarterly.data.api.route.lib.Verbiage.Race.{ASIAN, BLACK, HISPANIC, WHITE}

object MedianInterests extends InterestRatesGraph(
  "interest",
  "interest-rates",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE) {

  def getMedianInterestRatesSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE.toString, "interest-rates") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianInterestRates(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianInterestRates(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianInterestRates(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianInterestRates(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianInterestRates(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianInterestRates(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median interest rates changed?",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianInterestRatesCCByRaceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "interest-rates-cc-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median interest rates differed by race/ethnicity?",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianInterestRatesCCByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "interest-rates-cc-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median interest rates differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianInterestRatesCCByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "interest-rates-cc-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median interest rates differed by race/ethnicity? - Refinance",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianInterestRatesFHAByRaceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "interest-rates-fha-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how have median interest rates differed by race/ethnicity?",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianInterestRatesFHAByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "interest-rates-fha-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeHome(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how have median interest rates differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianInterestRatesFHAByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "interest-rates-fha-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how have median interest rates differed by race/ethnicity? - Refinance",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianInterestRatesLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "interest-rates-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeHome(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeHome(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeHome(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeHome(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeHome(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeHome(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median interest rates changed? - Home Purchase",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianInterestRatesLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "interest-rates-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeRefinance(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeRefinance(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeRefinance(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeRefinance(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeRefinance(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianInterestRatesLoanPurposeRefinance(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median interest rates changed? - Refinance",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }




}
