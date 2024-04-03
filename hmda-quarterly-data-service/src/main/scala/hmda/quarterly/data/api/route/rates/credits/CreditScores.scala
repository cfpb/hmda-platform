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

  def getMedianCreditScoresSummaryRoute: GraphRoute = new GraphRoute(MEDIAN_CREDIT_SCORES_TITLE, Category.BY_TYPE.toString, "credit-scores") {
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

  def getMedianCreditScoresCCByRaceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "credit-scores-cc-re") {
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
            CC_BY_RACE_TITLE,
            CC_BY_RACE_SUBTITLE,
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianCreditScoresCCByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "credit-scores-cc-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(Conventional, "a", conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(Conventional, "b", conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(Conventional, "h", conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(Conventional, "q", conforming = true)
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

  def getMedianCreditScoresCCByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "credit-scores-cc-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(Conventional, "a", conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(Conventional, "b", conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(Conventional, "h", conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(Conventional, "q", conforming = true)
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

  def getMedianCreditScoresFHAByRaceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "credit-scores-fha-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(FHAInsured, "a")
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(FHAInsured, "b")
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(FHAInsured, "h")
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(FHAInsured, "w")
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

  def getMedianCreditScoresFHAByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "credit-scores-fha-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(FHAInsured, "a")
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(FHAInsured, "b")
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(FHAInsured, "h")
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeHome(FHAInsured, "w")
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

  def getMedianCreditScoresFHAByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "credit-scores-fha-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(FHAInsured, "a")
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(FHAInsured, "b")
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(FHAInsured, "h")
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRaceLoanPurposeRefinance(FHAInsured, "w")
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

  def getMedianCreditScoresLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "credit-scores-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeLoanPurposeRefinance(Conventional, false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeLoanPurposeRefinance(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeLoanPurposeRefinance(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeLoanPurposeRefinance(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeLoanPurposeRefinance(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianCreditScoreByTypeLoanPurposeRefinance(VAGuaranteed, heloc = false, conforming = false)
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


}
