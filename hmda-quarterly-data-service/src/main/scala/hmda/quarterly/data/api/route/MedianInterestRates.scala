package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianInterestRates extends GraphRoute(
  "How have median interest rates changed?",
  "rate",
  "interest-rates"
) with JsonSupport {

  private def getMedianInterestRates(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianInterestRates(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getMedianInterestRates(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getMedianInterestRates(Conventional, "Conventional Non-Conforming")
          fha <- getMedianInterestRates(FHAInsured, "FHA")
          heloc <- getMedianInterestRates(Conventional,"HELOC", heloc = true)
          rhsfsa <- getMedianInterestRates(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getMedianInterestRates(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How have median interest rates changed?",
          "Median interest rates decreased overall for all loan types.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
