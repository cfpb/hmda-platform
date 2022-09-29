package hmda.quarterly.data.api.route.denials

import akka.http.scaladsl.server.Directives.{ complete, path, pathPrefix }
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphRoute, GraphSeriesSummary }
import hmda.quarterly.data.api.route.denials.Denials._
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object DenialRatesByType extends GraphRoute(
  DENIAL_RATES_TITLE,
  CATEGORY,
  "denials"
) with JsonSupport
  with Denials {

  private def getDenialRates(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchDenialRates(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getDenialRates(Conventional, CONVENTIONAL_CONFORMING, conforming = true)
          conventionalNonConforming <- getDenialRates(Conventional, CONVENTIONAL_NON_CONFORMING)
          fha <- getDenialRates(FHAInsured, FHA)
          heloc <- getDenialRates(Conventional, HELOC, heloc = true)
          rhsfsa <- getDenialRates(RHSOrFSAGuaranteed, RHS_FSA)
          va <- getDenialRates(VAGuaranteed, VA)
        } yield getDenialsGraphSeriesInfo(
          DENIAL_RATES_TITLE,
          DENIAL_RATES_SUBTITLE,
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
