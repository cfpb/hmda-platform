package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianTotalLoanCosts extends GraphRoute(
  "How have median total loan costs changed?",
  "quantity",
  "tlc"
) with JsonSupport {

  private def getMedianTotalLoanCosts(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianTotalLoanCosts(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getMedianTotalLoanCosts(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getMedianTotalLoanCosts(Conventional, "Conventional Non-Conforming")
          fha <- getMedianTotalLoanCosts(FHAInsured, "FHA")
          heloc <- getMedianTotalLoanCosts(Conventional,"HELOC", heloc = true)
          rhsfsa <- getMedianTotalLoanCosts(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getMedianTotalLoanCosts(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How have median total loan costs changed?",
          "Median total loan costs increased from 2018 to 2020 except for VA loans.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
