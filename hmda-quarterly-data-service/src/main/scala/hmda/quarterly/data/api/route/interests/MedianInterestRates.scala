package hmda.quarterly.data.api.route.interests

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphRoute, GraphSeriesSummary }
import hmda.quarterly.data.api.route.interests.InterestRates.{ CATEGORY, MEDIAN_SUBTITLE, MEDIAN_TITLE }
import hmda.quarterly.data.api.route.lib.Verbiage.Loan._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRates extends GraphRoute(
  MEDIAN_TITLE,
  CATEGORY,
  "interest-rates"
) with JsonSupport
  with InterestRates {

  private def getMedianInterestRates(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRates(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getMedianInterestRates(Conventional, CONVENTIONAL_CONFORMING, conforming = true)
          conventionalNonConforming <- getMedianInterestRates(Conventional, CONVENTIONAL_NON_CONFORMING)
          fha <- getMedianInterestRates(FHAInsured, FHA)
          heloc <- getMedianInterestRates(Conventional, HELOC, heloc = true)
          rhsfsa <- getMedianInterestRates(RHSOrFSAGuaranteed, RHS_FSA)
          va <- getMedianInterestRates(VAGuaranteed, VA)
        } yield getInterestRatesGraphSeriesInfo(
          MEDIAN_TITLE,
          MEDIAN_SUBTITLE,
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
