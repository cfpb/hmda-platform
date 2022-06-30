package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianCLTVFHAByRace extends GraphRoute(
  "For FHA loans, how has median CLTV differed by race/ethnicity?",
  "Combined Loan-to-Value Ratio",
  "ltv-fha-re"
) with JsonSupport {

  private def getMedianCLTV(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianCLTVByTypeByRace(FHAInsured, race, heloc = false, conforming = false)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path ("") {
      complete(
        for {
          asian <- getMedianCLTV("Asian", "a")
          black <- getMedianCLTV("Black", "b")
          hispanic <- getMedianCLTV("Hispanic", "h")
          white <- getMedianCLTV("White", "w")
        } yield GraphSeriesInfo(
          "For FHA loans, how has median CLTV differed by race/ethnicity?",
          "Both Blacks and Hispanics on average had the same median CLTV for FHA loans.",
          Seq(asian, black, hispanic, white),
          yLabel = "Median CLTV"
        )
      )
    }
  }
}
