package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object MedianDTIByType extends GraphRoute(
  "How has median DTI changed?",
  "rate",
  "dti"
) with JsonSupport {

  private def getMedianDTI(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchMedianDTIByType(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getMedianDTI(Conventional, "Conventional Conforming", conforming = true)
          conventionalNonConforming <- getMedianDTI(Conventional, "Conventional Non-Conforming")
          fha <- getMedianDTI(FHAInsured, "FHA")
          heloc <- getMedianDTI(Conventional,"HELOC", heloc = true)
          rhsfsa <- getMedianDTI(RHSOrFSAGuaranteed, "RHS/FSA")
          va <- getMedianDTI(VAGuaranteed, "VA")
        } yield GraphSeriesInfo(
          "How has median DTI changed?",
          "Conventional non-conforming loans on average had the lowest median DTI.",
          Seq(conventionalConforming, conventionalNonConforming, fha, heloc, rhsfsa, va)
        )
      )
    }
  }
}
