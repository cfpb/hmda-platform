package hmda.quarterly.data.api.route.denials

import akka.http.scaladsl.server.Directives.{ complete, path, pathPrefix }
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.FHAInsured
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphRoute, GraphSeriesInfo, GraphSeriesSummary }
import hmda.quarterly.data.api.route.lib.Verbiage.Race._
import hmda.quarterly.data.api.route.denials.Denials._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object DenialRatesFHAByRace extends GraphRoute(
  DENIAL_RATES_FHA_BY_RACE_TITLE,
  CATEGORY,
  "denials-fha-re"
) with JsonSupport
  with Denials {

  private def getDenialRates(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(FHAInsured, race, heloc = false, conforming = false)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getDenialRates(ASIAN, "a")
          black <- getDenialRates(BLACK, "b")
          hispanic <- getDenialRates(HISPANIC, "h")
          white <- getDenialRates(WHITE, "w")
        } yield getDenialsGraphSeriesInfo(
          DENIAL_RATES_FHA_BY_RACE_TITLE,
          DENIAL_RATES_FHA_BY_RACE_SUBTITLE,
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
