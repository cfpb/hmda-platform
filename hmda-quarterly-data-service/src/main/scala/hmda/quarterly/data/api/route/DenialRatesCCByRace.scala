package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object DenialRatesCCByRace extends GraphRoute(
  "For conventional conforming loans, how have denial rates differed by race/ethnicity?",
  "Denial Rates",
  "denials-cc-re"
) with JsonSupport {

  private def getDenialRates(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchDenialRatesByTypeByRace(Conventional, race, heloc = false, conforming = true)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getDenialRates("Asian", "a")
          black <- getDenialRates("Black", "b")
          hispanic <- getDenialRates("Hispanic", "h")
          white <- getDenialRates("White", "w")
        } yield GraphSeriesInfo(
          "For conventional conforming loans, how have denial rates differed by race/ethnicity?",
          "All racial and ethnic groups experienced an overall decrease in denial rates.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
