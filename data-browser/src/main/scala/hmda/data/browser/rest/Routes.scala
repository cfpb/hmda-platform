package hmda.data.browser.rest

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models._
import hmda.data.browser.rest.DataBrowserDirectives._
import hmda.data.browser.services.BrowserService
import io.circe.generic.auto._
import monix.execution.{Scheduler => MonixScheduler}

object Routes {
  def apply(browserService: BrowserService)(
      implicit scheduler: MonixScheduler): Route = {

    pathPrefix("view") {
      pathPrefix("nationwide") {
        extractFieldsForRawQueries { queryFields =>
          // GET /view/nationwide/csv
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/plain(UTF-8)`,
                csvSource(browserService.fetchData(queryFields))
              )
            )
          } ~
            // GET /view/nationwide/pipe
            (path("pipe") & get) {
              complete(
                HttpEntity(
                  `text/plain(UTF-8)`,
                  pipeSource(browserService.fetchData(queryFields))
                )
              )
            }
        } ~
          // GET /view/nationwide/aggregations
          (path("aggregations") & get) {
            extractFieldsForAggregation { queryFields =>
              val allFields = queryFields
              complete(
                browserService
                  .fetchAggregate(allFields)
                  .map(aggs =>
                    AggregationResponse(Parameters.fromBrowserFields(allFields),
                                        aggs))
                  .runToFuture)
            }
          }
      } ~
        // GET /view/aggregations
        (path("aggregations") & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            extractFieldsForAggregation { remainingQueryFields =>
              val allFields = mandatoryFields ++ remainingQueryFields
              complete(
                browserService
                  .fetchAggregate(allFields)
                  .map(aggs =>
                    AggregationResponse(Parameters.fromBrowserFields(allFields),
                                        aggs))
                  .runToFuture
              )
            }
          }
        } ~
        // GET /view/csv
        (path("csv") & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            extractFieldsForRawQueries { remainingQueryFields =>
              complete(
                HttpEntity(`text/plain(UTF-8)`,
                           csvSource(browserService.fetchData(
                             mandatoryFields ++ remainingQueryFields))))
            }
          }
        } ~
        // GET /view/pipe
        (path("pipe") & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            extractFieldsForRawQueries { remainingQueryFields =>
              complete(
                HttpEntity(`text/plain(UTF-8)`,
                           pipeSource(browserService.fetchData(
                             mandatoryFields ++ remainingQueryFields))))
            }
          }
        }
    }
  }
}
