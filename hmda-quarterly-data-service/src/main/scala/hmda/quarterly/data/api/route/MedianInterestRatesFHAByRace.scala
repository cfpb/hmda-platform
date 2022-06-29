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
  "For FHA loans, how have median interest rates differed by race/ethnicity?",
  "Interest Rates",
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
          "By FHA loans, how have median interest rates differed by race/ethnicity?",
          "Interest rates decreased from 2018 quarter four to 2020.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
