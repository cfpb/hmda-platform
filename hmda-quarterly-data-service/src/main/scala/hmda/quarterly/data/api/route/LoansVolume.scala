package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.{ Conventional, FHAInsured, LoanTypeEnum, RHSOrFSAGuaranteed, VAGuaranteed }
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.lib.Labels.LOANS
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object LoansVolume extends GraphRoute(
  "How has the number of loans changed?",
  "Loan & Application Counts",
  "loans"
) with JsonSupport {

  private def getVolume(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchLoansVolumeByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getVolume(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getVolume(Conventional, "Conventional Non-Conforming")
          fha <- getVolume(FHAInsured, "FHA")
          heloc <- getVolume(Conventional, "HELOC", heloc = true)
          rhsfsa <- getVolume(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getVolume(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How has the number of loans changed?",
          "Conventional conforming loans increased starting in 2019 and the increase quickened in 2020. VA loans have also been steadily increasing since 2019.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va),
          yLabel = LOANS
        )
      )
    }
  }
}
