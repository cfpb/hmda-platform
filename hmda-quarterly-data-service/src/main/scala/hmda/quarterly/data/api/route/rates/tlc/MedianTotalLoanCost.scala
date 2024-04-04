package hmda.quarterly.data.api.route.rates.tlc

import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.route.rates.RatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.Scheduler.Implicits.global
import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.{Conventional, FHAInsured, RHSOrFSAGuaranteed, VAGuaranteed}
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute}
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType.{CONVENTIONAL_CONFORMING, CONVENTIONAL_NON_CONFORMING, FHA, HELOC, RHS_FSA, VA}
import hmda.quarterly.data.api.route.lib.Verbiage.Race.{ASIAN, BLACK, HISPANIC, WHITE}

object MedianTotalLoanCost extends RatesGraph(
  "tlc",
  "tlc",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE_NO_HELOC) {

  def getMedianTotalLoanCostsSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "tlc") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianTotalLoanCosts(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianTotalLoanCosts(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianTotalLoanCosts(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianTotalLoanCosts(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianTotalLoanCosts(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianTotalLoanCosts(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median total loan costs changed?",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsCCByRaceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "tlc-cc-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median total loan costs differed by race/ethnicity?",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsCCByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "tlc-cc-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median total loan costs differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsCCByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "tlc-cc-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median total loan costs differed by race/ethnicity? - Refinance",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsFHAByRaceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "tlc-fha-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(FHAInsured, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(FHAInsured, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(FHAInsured, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(FHAInsured, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how have median total loan costs differed by race/ethnicity? - Refinance",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsFHAByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "tlc-fha-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeHome(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how have median total loan costs differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsFHAByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "tlc-fha-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(FHAInsured, "a", heloc = false, conforming = false)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(FHAInsured, "b", heloc = false, conforming = false)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(FHAInsured, "h", heloc = false, conforming = false)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRaceLoanPurposeRefinance(FHAInsured, "w", heloc = false, conforming = false)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how have median total loan costs differed by race/ethnicity? - Refinance",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "tlc-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeHome(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeHome(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeHome(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeHome(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeHome(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeHome(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median total loan costs changed? - Home Purchase",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianTotalLoanCostsLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "tlc-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeRefinance(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeRefinance(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeRefinance(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeRefinance(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeRefinance(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianTotalLoanCostsLoanPurposeRefinance(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median total loan costs changed? - Refinance",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

}
