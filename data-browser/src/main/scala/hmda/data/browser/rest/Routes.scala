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
        extractBrowserFieldsForCsv { browserFields =>
          (path("csv") & get) {
            complete(
              HttpEntity(
                `text/plain(UTF-8)`,
                csvSource(browserService.fetchData(browserFields))
              )
            )
          }
        }
      } ~ pathPrefix("aggregations") {
        extractMsaAndStateBrowserFields { (states, msaMds) =>
          extractBrowserFieldsForAggregation { browserFields =>
            val allFields = states :: msaMds :: browserFields
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
      } ~ (path("csv") & get) {
        extractMsaAndStateBrowserFields { (states, msaMds) =>
          extractBrowserFieldsForCsv { browserFields =>
            complete(
              HttpEntity(
                `text/plain(UTF-8)`,
                csvSource(
                  browserService.fetchData(states :: msaMds :: browserFields))))
          }
        }
      }
    }
  }
}