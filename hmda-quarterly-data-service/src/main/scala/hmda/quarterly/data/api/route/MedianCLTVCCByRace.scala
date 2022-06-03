package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianCLTVCCByRace extends GraphRoute(
  "For conventional conforming loans, how has median CLTV differed by race/ethnicity?",
  "rate",
  "ltv-cc-race"
) with JsonSupport {

  private def getMedianCLTV(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianCLTVByTypeByRace(Conventional, race, heloc = false, conforming = true)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getMedianCLTV("Asian", "a")
          black <- getMedianCLTV("Black", "b")
          hispanic <- getMedianCLTV("Hispanic", "h")
          white <- getMedianCLTV("White", "w")
        } yield GraphSeriesInfo(
          "For conventional conforming loans, how has median CLTV differed by race/ethnicity?",
          "In 2019, all racial and ethnicity groups experienced a decline in median CLTV.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
