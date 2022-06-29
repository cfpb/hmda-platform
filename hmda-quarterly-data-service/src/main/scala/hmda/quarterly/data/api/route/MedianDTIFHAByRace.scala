package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianDTIFHAByRace extends GraphRoute(
  "For FHA loans, how has median DTI differed by race/ethnicity?",
  "Debt-to-Income Ratio",
  "dti-fha-re"
) with JsonSupport {

  private def getMedianCLTV(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianDTIByTypeByRace(FHAInsured, race, heloc = false, conforming = false)
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
          "For FHA loans, how has median DTI differed by race/ethnicity?",
          "For FHA loans, whites had the lowest median DTI while the position of other racial and ethnic groups changed.",
          Seq(asian, black, hispanic, white)
        )
      )
    }
  }
}
