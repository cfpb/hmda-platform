package hmda.quarterly.data.api.route.interests

import akka.http.scaladsl.server.Directives.{ complete, path, pathPrefix }
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.FHAInsured
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphRoute, GraphSeriesSummary }
import hmda.quarterly.data.api.route.interests.InterestRates.{ CATEGORY, MEDIAN_FHA_SUBTITLE, MEDIAN_FHA_TITLE }
import hmda.quarterly.data.api.route.lib.Verbiage.Race._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRatesFHAByRace extends GraphRoute(
  MEDIAN_FHA_TITLE,
  CATEGORY,
  "interest-rates-fha-re"
) with JsonSupport
  with InterestRates {

  private def getMedianInterestRates(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(FHAInsured, race, heloc = false, conforming = false)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getMedianInterestRates(ASIAN, "a")
          black <- getMedianInterestRates(BLACK, "b")
          hispanic <- getMedianInterestRates(HISPANIC, "h")
          white <- getMedianInterestRates(WHITE, "w")
        } yield getInterestRatesGraphSeriesInfo(
          MEDIAN_FHA_TITLE,
          MEDIAN_FHA_SUBTITLE,
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
