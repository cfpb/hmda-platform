package hmda.quarterly.data.api.route.rates.credits

import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute, GraphSeriesInfo, GraphSeriesSummary}
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType.{CONVENTIONAL_CONFORMING, CONVENTIONAL_NON_CONFORMING, FHA, HELOC, RHS_FSA, VA}
import hmda.quarterly.data.api.route.lib.Verbiage.Race.{ASIAN, BLACK, HISPANIC, WHITE}
import hmda.quarterly.data.api.route.rates.CountRatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.Scheduler.Implicits.global
import hmda.quarterly.data.api.route.lib.Verbiage.Title._
import hmda.quarterly.data.api.route.lib.Verbiage.Category._
import hmda.quarterly.data.api.route.lib.Verbiage.Endpoint._

object CreditScores extends CountRatesGraph(
  "credit",
  "credit-scores",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE) {

  def getMedianCreditScoresSummaryRoute: GraphRoute = new GraphRoute(MEDIAN_CREDIT_SCORES_TITLE, CREDIT_CATEGORY, MEDIAN_CREDIT_SCORES_ENDPOINT) {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(VAGuaranteed, heloc = false, conforming = false)
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

  def getMedianCreditScoresCCByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "a", conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "b", conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "h", conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "q", conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianCreditScoresCCByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(VAGuaranteed, heloc = false, conforming = false)
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

  def getMedianCreditScoresFHAByRaceSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "a", conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "b", conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "h", conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "q", conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianCreditScoresFHAByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(VAGuaranteed, heloc = false, conforming = false)
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

  def getMedianCreditScoresFHAByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "a", conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "b", conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "h", conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "q", conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianCreditScoresLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianCreditScoreByType(VAGuaranteed, heloc = false, conforming = false)
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

  def getMedianCreditScoresLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute("ACTUAL_TITLE", "ACTUAL_CATEGORY_VERBIAGE", "ACTUAL_ENDPOINT") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "a", conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "b", conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "h", conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, "q", conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            BY_TYPE_TITLE,
            BY_TYPE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }
}
