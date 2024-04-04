package hmda.quarterly.data.api.route.rates.dti

import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.{Conventional, FHAInsured, RHSOrFSAGuaranteed, VAGuaranteed}
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute}
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType.{CONVENTIONAL_CONFORMING, CONVENTIONAL_NON_CONFORMING, FHA, HELOC, RHS_FSA, VA}
import hmda.quarterly.data.api.route.rates.RatesGraph
import hmda.quarterly.data.api.route.rates.RatesGraph._
import monix.execution.Scheduler.Implicits.global
import hmda.quarterly.data.api.route.lib.Verbiage.Race.{ASIAN, BLACK, HISPANIC, WHITE}

object MedianDTI extends RatesGraph(
  "dti",
  "dti",
  BY_TYPE_TITLE,
  BY_TYPE_SUBTITLE,
  Category.BY_TYPE) {

  def getMedianDTIByTypeSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE.toString, "dti") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianDTIByType(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianDTIByType(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianDTIByType(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianDTIByType(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianDTIByType(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianDTIByType(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median DTIs changed?",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianDTIByTypeLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "dti-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeHome(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median DTIs changed? - Home Purchase",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianDTIByTypeLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(BY_TYPE_TITLE, Category.BY_TYPE_NO_HELOC.toString, "dti-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            conventionalConforming <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeRefinance(Conventional, heloc = false, conforming = true)
              .map(convertToGraph(CONVENTIONAL_CONFORMING, _)).runToFuture
            conventionalNonConforming <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeRefinance(Conventional, heloc = false, conforming = false)
              .map(convertToGraph(CONVENTIONAL_NON_CONFORMING, _)).runToFuture
            fha <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeRefinance(FHAInsured, heloc = false, conforming = false)
              .map(convertToGraph(FHA, _)).runToFuture
            heloc <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeRefinance(Conventional, heloc = true, conforming = false)
              .map(convertToGraph(HELOC, _)).runToFuture
            rhsfsa <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeRefinance(RHSOrFSAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(RHS_FSA, _)).runToFuture
            va <- QuarterlyGraphRepo.fetchMedianDTIByTypeLoanPurposeRefinance(VAGuaranteed, heloc = false, conforming = false)
              .map(convertToGraph(VA, _)).runToFuture
          } yield getGraphSeriesInfo(
            "How have median DTIs changed? - Refinance",
            "",
            Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
          )
        )
      }
    }
  }

  def getMedianDTICCByRaceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "dti-cc-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how has median DTI differed by race/ethnicity?",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianDTICCByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "dti-cc-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how has median DTI differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianDTICCByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(CC_BY_RACE_TITLE, Category.BY_RACE.toString, "dti-cc-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeRefinance(Conventional, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeRefinance(Conventional, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeRefinance(Conventional, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeRefinance(Conventional, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For conventional conforming loans, how has median DTI differed by race/ethnicity? - Refinance",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianDTIFHAByRaceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "dti-fha-re") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(FHAInsured, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(FHAInsured, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(FHAInsured, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(FHAInsured, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how has median DTI differed by race/ethnicity?",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianDTIFHAByRaceLoanPurposeHomeSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "dti-fha-re-loan-purpose-home") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how has median DTI differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }

  def getMedianDTIFHAByRaceLoanPurposeRefinanceSummaryRoute: GraphRoute = new GraphRoute(FHA_BY_RACE_TITLE, Category.BY_RACE.toString, "dti-fha-re-loan-purpose-refinance") {
    override def route: Route = pathPrefix(endpoint) {
      path("") {
        complete(
          for {
            asian <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "a", heloc = false, conforming = true)
              .map(convertToGraph(ASIAN, _)).runToFuture
            black <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "b", heloc = false, conforming = true)
              .map(convertToGraph(BLACK, _)).runToFuture
            hispanic <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "h", heloc = false, conforming = true)
              .map(convertToGraph(HISPANIC, _)).runToFuture
            white <- QuarterlyGraphRepo.fetchMedianDTIByTypeByRaceLoanPurposeHome(FHAInsured, "w", heloc = false, conforming = true)
              .map(convertToGraph(WHITE, _)).runToFuture
          } yield getGraphSeriesInfo(
            "For FHA loans, how has median DTI differed by race/ethnicity? - Home Purchase",
            "",
            Seq(asian, black, hispanic, white)
          )
        )
      }
    }
  }
}
