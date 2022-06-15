package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRatesFHAByRace extends GraphRoute(
  "For FHA loans, how has median DTI differed by race/ethnicity?",
  "rate",
  "interest-rates-fha-re"
) with JsonSupport {

  private def getMedianInterestRates(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRatesByTypeByRace(FHAInsured, race, heloc = false, conforming = false)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path ("") {
      complete(
        for {
          asian <- getMedianInterestRates("Asian", "a")
          black <- getMedianInterestRates("Black", "b")
          hispanic <- getMedianInterestRates("Hispanic", "h")
          white <- getMedianInterestRates("White", "w")
        } yield GraphSeriesInfo(
          "For FHA loans, how has median DTI differed by race/ethnicity?",
          "For FHA loans, whites had the lowest median DTI while the position of other racial and ethnic groups changed.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
