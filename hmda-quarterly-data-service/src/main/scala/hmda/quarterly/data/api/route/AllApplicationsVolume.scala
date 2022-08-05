package hmda.quarterly.data.api.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.quarterly.data.api.dao.repo.QuarterlyGraphRepo
import hmda.quarterly.data.api.dto.QuarterGraphData._
import hmda.quarterly.data.api.route.lib.Labels.APPS
import hmda.quarterly.data.api.serde.JsonSupport
import monix.execution.CancelableFuture
import monix.execution.Scheduler.Implicits.global

object AllApplicationsVolume extends GraphRoute(
  "How much of the total loan/application count do quarterly filers account for?",
  "Loan & Application Counts",
  "all-applications"
) with JsonSupport {
  private def getVolume(title: String, quarterly: Boolean = true): CancelableFuture[GraphSeriesSummary] =
    QuarterlyGraphRepo.fetchTotalApplicationsVolume(quarterly)
      .map(convertToGraph(title, _)).runToFuture

  override def route: Route = pathPrefix(endpoint) {
    path("") {
      complete(
        for {
          quarterlyFilers <- getVolume("Quarterly Filers")
          allFilers <- getVolume("All HMDA Filers", quarterly = false)
        } yield GraphSeriesInfo(
          "How much of the total loan/application count do quarterly filers account for?",
          "How does the number of loans and applications submitted from quarterly filers compare to the total amount?",
          Seq(quarterlyFilers, allFilers),
          yLabel = APPS
        )
      )
    }
  }
}
