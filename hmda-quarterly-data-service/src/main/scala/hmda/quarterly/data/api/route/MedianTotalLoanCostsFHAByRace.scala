package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.lib.Labels.TLC
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianTotalLoanCostsFHAByRace extends GraphRoute(
  "For FHA loans, how have median total loan costs differed by race/ethnicity?",
  "Total Loan Costs",
  "tlc-fha-re"
) with JsonSupport {

  private def getMedianTotalLoanCosts(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(FHAInsured, race, heloc = false, conforming = false)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path ("") {
      complete(
        for {
          asian <- getMedianTotalLoanCosts("Asian", "a")
          black <- getMedianTotalLoanCosts("Black", "b")
          hispanic <- getMedianTotalLoanCosts("Hispanic", "h")
          white <- getMedianTotalLoanCosts("White", "w")
        } yield GraphSeriesInfo(
          "For FHA loans, how have median total loan costs differed by race/ethnicity?",
          "Median total loan costs increased from the start of 2018 to 2020.",
          Seq(asian, black, hispanic, white),
          yLabel = TLC
        )
      )
    }
  }
}
