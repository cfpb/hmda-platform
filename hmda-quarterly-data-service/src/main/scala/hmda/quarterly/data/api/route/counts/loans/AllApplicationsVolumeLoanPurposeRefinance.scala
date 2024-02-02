package hmda.quarterly.data.api.route.counts.loans

import akka.http.scaladsl.server.Directives.{complete, path, pathPrefix}
import akka.http.scaladsl.server.Route
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData.{GraphRoute, GraphSeriesInfo, GraphSeriesSummary}
import hmda.quarterly.data.api.route.counts.loans.Loans._
import hmda.quarterly.data.api.route.lib.Verbiage.COUNT_DECIMAL_PRECISION
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object AllApplicationsVolumeLoanPurposeRefinance extends GraphRoute(
  ALL_APPS_VOLUME_TITLE_REFINANCE,
  CATEGORY,
  "all-applications-loan-purpose-refinance"
) with JsonSupport {
  private def getVolume(title: String, quarterly: Boolean = true): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchTotalApplicationsVolumeLoanPurposeRefinance(quarterly)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          quarterlyFilers <- getVolume(QUARTERLY_FILERS_LABEL)
          allFilers <- getVolume(ALL_FILERS_LABEL, quarterly = false)
        } yield GraphSeriesInfo(
          ALL_APPS_VOLUME_TITLE_REFINANCE,
          ALL_APPS_VOLUME_SUBTITLE,
          Seq(quarterlyFilers, allFilers),
          yLabel = APP_LABEL,
          decimalPrecision = COUNT_DECIMAL_PRECISION
        )
      )
    }
  }
}
