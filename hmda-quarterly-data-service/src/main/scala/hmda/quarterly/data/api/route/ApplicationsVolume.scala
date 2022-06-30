package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.{ Conventional, FHAInsured, LoanTypeEnum, RHSOrFSAGuaranteed, VAGuaranteed }
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.lib.Labels.APPS
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object ApplicationsVolume extends GraphRoute(
  "How has the number of applications changed?",
  "Loan & Application Counts",
  "applications"
) with JsonSupport {
  private def getVolume(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchApplicationsVolumeByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getVolume(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getVolume(Conventional, "Conventional Non-Conforming")
          fha <- getVolume(FHAInsured, "FHA")
          heloc <- getVolume(Conventional,"HELOC", heloc = true)
          rhsfsa <- getVolume(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getVolume(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How has the number of applications changed?",
          "Conventional conforming applications dramatically increased since 2019. FHA loans temporarily moved higher in 2020 Q3.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va),
          yLabel = APPS
        )
      )
    }
  }
}
