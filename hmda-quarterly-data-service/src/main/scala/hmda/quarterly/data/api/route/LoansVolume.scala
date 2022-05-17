package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums.{ Conventional, FHAInsured, LoanTypeEnum, RHSOrFSAGuaranteed, VAGuaranteed }
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object LoansVolume extends GraphRoute(
  "How has the number of loans changed?",
  "quantity",
  "loans"
) with JsonSupport {

  private def getVolume(loanType: LoanTypeEnum, heloc: Boolean, title: String): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchLoansVolumeByType(loanType, heloc)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventional <- getVolume(Conventional, false, "Conventional")
          fha <- getVolume(FHAInsured, false, "FHA")
          heloc <- getVolume(Conventional, true, "HELOC")
          rhsfsa <- getVolume(RHSOrFSAGuaranteed, false, "RHS/FSA")
          va <- getVolume(VAGuaranteed, false, "VA")
        } yield GraphSeriesInfo(
          "How has the number of loans changed?",
          "Conventional conforming loans increased starting in 2019 and the increase quickened in 2020. VA loans have also been steadily increasing since 2019.",
          Seq(conventional, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
