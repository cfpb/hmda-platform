package hmda.quarterly.data.api.route.interests

import akka.http.scaladsl.server.Directives.{ complete, path, pathPrefix }
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.Conventional
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{ GraphRoute, GraphSeriesSummary }
import hmda.quarterly.data.api.route.interests.InterestRates.{ CATEGORY, MEDIAN_CC_SUBTITLE, MEDIAN_CC_TITLE }
import hmda.quarterly.data.api.route.lib.Verbiage.Race._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRatesCCByRace extends GraphRoute(
  MEDIAN_CC_TITLE,
  CATEGORY,
  "interest-rates-cc-re"
) with JsonSupport
  with InterestRates {

  private def getMedianInterestRates(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(Conventional, race, heloc = false, conforming = true)
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
          MEDIAN_CC_TITLE,
          MEDIAN_CC_SUBTITLE,
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
