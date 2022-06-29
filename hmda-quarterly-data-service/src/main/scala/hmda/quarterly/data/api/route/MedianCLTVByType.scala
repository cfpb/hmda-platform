package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianCLTVByType extends GraphRoute(
  "How has median CLTV changed?",
  "Combined Loan-to-Value Ratio",
  "ltv"
) with JsonSupport {

  private def getMedianCLTV(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianCLTVByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getMedianCLTV(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getMedianCLTV(Conventional, "Conventional Non-Conforming")
          fha <- getMedianCLTV(FHAInsured, "FHA")
          heloc <- getMedianCLTV(Conventional,"HELOC", heloc = true)
          rhsfsa <- getMedianCLTV(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getMedianCLTV(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How has median CLTV changed?",
          "RHS/FSA loans had the highest median CLTV while HELOCs had generally the lowest.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
