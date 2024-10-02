package hmda.quarterly.data.api.route.counts.loans

import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.model.filing.lar.enums._
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute, GraphSeriesInfo, GraphSeriesSummary}
import hmda.quarterly.data.api.route.counts.loans.Loans.{CATEGORY, LOAN_LABEL, LOAN_VOLUME_SUBTITLE, LOAN_VOLUME_TITLE_HOME}
import hmda.quarterly.data.api.route.lib.Verbiage.COUNT_DECIMAL_PRECISION
import hmda.quarterly.data.api.route.lib.Verbiage.LoanType._
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object LoansVolumeLoanPurposeHome extends GraphRoute(
  LOAN_VOLUME_TITLE_HOME,
  CATEGORY,
  "loans-loan-purpose-home"
) with JsonSupport {

  private def getVolume(loanType: LoanTypeEnum, title: String, heloc: Boolean = false, conforming: Boolean = false): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchLoansVolumeByTypeLoanPurposeHome(loanType, heloc, conforming)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          conventionalConforming <- getVolume(Conventional, CONVENTIONAL_CONFORMING, conforming = true)
          conventionalNonConforming <- getVolume(Conventional, CONVENTIONAL_NON_CONFORMING)
          fha <- getVolume(FHAInsured, FHA)
          rhsfsa <- getVolume(RHSOrFSAGuaranteed, RHS_FSA)
          va <- getVolume(VAGuaranteed, VA)
        } yield GraphSeriesInfo(
          LOAN_VOLUME_TITLE_HOME,
          LOAN_VOLUME_SUBTITLE,
          Seq(conventionalConforming, conventionalNonConforming, fha, rhsfsa, va),
          yLabel = LOAN_LABEL,
          decimalPrecision = COUNT_DECIMAL_PRECISION
        )
      )
    }
  }
}
