package hmda.data.browser.rest

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes.Found
import akka.http.scaladsl.model.{HttpEntity, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.Settings
import hmda.data.browser.models._
import hmda.data.browser.rest.DataBrowserDirectives._
import hmda.data.browser.services.BrowserService
import io.circe.generic.auto._
import monix.execution.{Scheduler => MonixScheduler}
import org.slf4j.LoggerFactory

object Routes {
  def apply(browserService: BrowserService, settings: Settings)(
      implicit scheduler: MonixScheduler): Route = {


    val log = LoggerFactory.getLogger("data-browser-api")
    val routeConf = settings.routes
    val Csv = "csv"
    val Pipe = "pipe"
    val Aggregations = "aggregations"

    pathPrefix("view") {
      pathPrefix("nationwide") {
        extractFieldsForRawQueries { queryFields =>
          // GET /view/nationwide/csv
          contentDisposition(queryFields) {
            (path(Csv) & get) {
              if (queryFields.isEmpty)
                redirect(Uri(routeConf.nationwideCsv), Found)
              else
                complete(
                  HttpEntity(
                    `text/plain(UTF-8)`,
                    csvSource(browserService.fetchData(queryFields))
                  )
                )
            }
          } ~
            // GET /view/nationwide/pipe
            (path(Pipe) & get) {
              contentDisposition(queryFields) {
                if (queryFields.isEmpty)
                  redirect(Uri(routeConf.nationwidePipe), Found)
                else
                  complete(
                    HttpEntity(
                      `text/plain(UTF-8)`,
                      pipeSource(browserService.fetchData(queryFields))
                    )
                  )
              }
            }
        } ~
          // GET /view/nationwide/aggregations
          (path(Aggregations) & get) {
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
        (path(Aggregations) & get) {
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
        (path(Csv) & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            extractFieldsForRawQueries { remainingQueryFields =>
              contentDisposition(mandatoryFields ++ remainingQueryFields) {
                complete(
                  HttpEntity(`text/plain(UTF-8)`,
                             csvSource(browserService.fetchData(
                               mandatoryFields ++ remainingQueryFields))))
              }
            }
          }
        } ~
        // GET /view/pipe
        (path(Pipe) & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            extractFieldsForRawQueries { remainingQueryFields =>
              contentDisposition(mandatoryFields ++ remainingQueryFields) {
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
}
