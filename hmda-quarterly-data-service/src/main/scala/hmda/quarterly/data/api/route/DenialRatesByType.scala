package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object DenialRatesByType extends GraphRoute(
  "How have denial rates changed?",
  "Denial Rates",
  "denials"
) with JsonSupport {

  private def getDenialRates(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchDenialRates(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getDenialRates(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getDenialRates(Conventional, "Conventional Non-Conforming")
          fha <- getDenialRates(FHAInsured, "FHA")
          heloc <- getDenialRates(Conventional,"HELOC", heloc = true)
          rhsfsa <- getDenialRates(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getDenialRates(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How have denial rates changed?",
          "HELOC loans had the highest denial rate.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va),
          yLabel = "Denial Rate Percentage"
        )
      )
    }
  }
}
