package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianCreditScoresCCByRace extends GraphRoute(
  "For conventional conforming loans, how have median credit scores differed by race/ethnicity?",
  "rate",
  "credit-scores-cc-race-ethnicity"
) with JsonSupport {

  private def getMedianScore(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianCreditScoreByTypeByRace(Conventional, race, conforming = true)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getMedianScore("Asian", "a")
          black <- getMedianScore("Black", "b")
          hispanic <- getMedianScore("Hispanic", "h")
          white <- getMedianScore("White", "w")
        } yield GraphSeriesInfo(
          "For conventional conforming loans, how have median credit scores differed by race/ethnicity?",
          "In 2019, median credit scores increased for all racial and ethnic groups.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
