package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianTotalLoanCostsCCByRace extends GraphRoute(
  "For conventional conforming loans, how have median total loan costs differed by race/ethnicity?",
  "Total Loan Costs",
  "tlc-cc-re"
) with JsonSupport {

  private def getMedianTotalLoanCosts(title: String, race: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianTotalLoanCostsByTypeByRace(Conventional, race, heloc = false, conforming = true)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          asian <- getMedianTotalLoanCosts("Asian", "a")
          black <- getMedianTotalLoanCosts("Black", "b")
          hispanic <- getMedianTotalLoanCosts("Hispanic", "h")
          white <- getMedianTotalLoanCosts("White", "w")
        } yield GraphSeriesInfo(
          "For conventional conforming loans, how have median total loan costs differed by race/ethnicity?",
          "Since 2019, all median interest rates have decreased.",
          Seq(asian, black, hispanic, white),
          yLabel = "Median Total Loan Costs"
        )
      )
    }
  }
}
