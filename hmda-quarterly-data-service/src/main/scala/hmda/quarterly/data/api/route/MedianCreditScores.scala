package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianCreditScores extends GraphRoute(
  "How have median credit scores changed?",
  "Credit Score",
  "credit-scores"
) with JsonSupport {

  private def getMedianScore(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianCreditScoreByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getMedianScore(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getMedianScore(Conventional, "Conventional Non-Conforming")
          fha <- getMedianScore(FHAInsured, "FHA")
          heloc <- getMedianScore(Conventional, "HELOC", heloc = true)
          rhsfsa <- getMedianScore(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getMedianScore(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How have median credit scores changed?",
          "HELOC loans had the highest median credit scores while FHA loans had the lowest.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
