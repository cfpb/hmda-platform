package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRatesCCByRace extends GraphRoute(
  "For conventional conforming loans, how has median DTI differed by race/ethnicity?",
  "rate",
  "interest-rates-cc-re"
) with JsonSupport {

  private def getMedianInterestRates(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(Conventional, race, heloc = false, conforming = true)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getMedianInterestRates("Asian", "a")
          black <- getMedianInterestRates("Black", "b")
          hispanic <- getMedianInterestRates("Hispanic", "h")
          white <- getMedianInterestRates("White", "w")
        } yield GraphSeriesInfo(
          "For conventional conforming loans, how has median DTI differed by race/ethnicity?",
          "For conventional conforming loans, Hispanics had the highest median DTI while whites had the lowest.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
