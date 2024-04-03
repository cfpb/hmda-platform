package hmda.quarterly.data.api.route.rates.denials

import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute}
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType.{CONVENTIONAL_CONFORMING, CONVENTIONAL_NON_CONFORMING, FHA, HELOC, RHS_FSA, VA}
import hmda.quarterly.data.api.route.rates.RatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.Scheduler.Implicits.global
import hmda.quarterly.data.api.route.lib.Verbiage.Race.{ASIAN, BLACK, HISPANIC, WHITE}


object Denials extends RatesGraph(
  "denial",
  "denials",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE) {

  def getDenialRatesByTypeSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE.toString, "denials") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchDenialRates(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchDenialRates(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchDenialRates(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchDenialRates(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchDenialRates(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchDenialRates(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getDenialRatesByTypeLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "denials-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeHome(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeHome(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeHome(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeHome(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeHome(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeHome(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getDenialRatesByTypeLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "denials-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeRefinance(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeRefinance(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeRefinance(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeRefinance(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeRefinance(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchDenialRatesLoanPurposeRefinance(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getDenialRatesCCByRaceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "denials-cc-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            CC_BY_RACE_TITLE,
            CC_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getDenialRatesCCByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "denials-cc-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            CC_BY_RACE_TITLE,
            CC_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getDenialRatesCCByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "denials-cc-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            CC_BY_RACE_TITLE,
            CC_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getDenialRatesFHAByRaceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "denials-fha-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            FHA_BY_RACE_TITLE,
            FHA_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getDenialRatesFHAByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "denials-fha-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeHome(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            FHA_BY_RACE_TITLE,
            FHA_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getDenialRatesFHAByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "denials-fha-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchDenialRatesByTypeByRaceLoanPurposeRefinance(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            FHA_BY_RACE_TITLE,
            FHA_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

}
