package hmda.data.browser.rest

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.data.browser.models._
import hmda.data.browser.rest.DataBrowserDirectives._
import hmda.data.browser.services.BrowserService
import io.circe.generic.auto._
import monix.execution.{Scheduler => MonixScheduler}
import org.slf4j.LoggerFactory

object Routes {

  val log = LoggerFactory.getLogger("data-browser-api")
  def apply(browserService: BrowserService)(
      implicit scheduler: MonixScheduler): Route = {

    pathPrefix("view") {
      pathPrefix("nationwide") {
        extractFieldsForRawQueries { queryFields =>
          // GET /view/nationwide/csv
          val filename = "nationwide" + queryFields
            .map(mc => mc.name + "_" + mc.values.mkString("-"))
            .mkString("_")
          respondWithHeader(
            RawHeader("Content-Disposition",
                      s"""attachment; filename="${filename}.csv"""")) {
            (path("csv") & get) {
              extractNationwideMandatoryYears { mandatoryFields =>
                val allFields = queryFields ++ mandatoryFields
                log.info("Nationwide [CSV]: " + allFields)
                complete(
                  HttpEntity(
                    `text/plain(UTF-8)`,
                    csvSource(browserService.fetchData(allFields))
                  )
                )
              }
            }
          } ~
            // GET /view/nationwide/pipe
            (path("pipe") & get) {
              log.info("Nationwide [Pipe]: " + queryFields)
              val filename = "nationwide" + queryFields
                .map(mc => mc.name + "_" + mc.values.mkString("-"))
                .mkString("_")
              extractNationwideMandatoryYears { mandatoryFields =>
                val allFields = queryFields ++ mandatoryFields
                respondWithHeader(
                  RawHeader("Content-Disposition",
                            s"""attachment; filename="${filename}.txt"""")) {
                  complete(
                    HttpEntity(
                      `text/plain(UTF-8)`,
                      pipeSource(browserService.fetchData(allFields))
                    )
                  )
                }
              }
            }
        } ~
          // GET /view/nationwide/aggregations
          (path("aggregations") & get) {
            extractNationwideMandatoryYears { mandatoryFields =>
              extractFieldsForAggregation { queryFields =>
                val allFields = mandatoryFields ++ queryFields
                log.info("Nationwide [Aggregations]: " + allFields)
                complete(browserService
                  .fetchAggregate(allFields)
                  .map(aggs =>
                    AggregationResponse(Parameters.fromBrowserFields(allFields),
                                        aggs))
                  .runToFuture)
              }
            }
          }
      } ~
        // GET /view/aggregations
        (path("aggregations") & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            extractFieldsForAggregation { remainingQueryFields =>
              val allFields = mandatoryFields ++ remainingQueryFields
              log.info("Aggregations: " + allFields)
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
            val filename = mandatoryFields
              .map(mc => mc.name + "_" + mc.values.mkString("-"))
              .mkString("_")
            respondWithHeader(
              RawHeader("Content-Disposition",
                        s"""attachment; filename="${filename}.csv"""")) {
              extractFieldsForRawQueries { remainingQueryFields =>
                val allFields = mandatoryFields ++ remainingQueryFields
                log.info("CSV: " + allFields)
                complete(
                  HttpEntity(`text/plain(UTF-8)`,
                             csvSource(browserService.fetchData(allFields))))
              }
            }
          }
        } ~
        // GET /view/pipe
        (path("pipe") & get) {
          extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
            val filename = mandatoryFields
              .map(mc => mc.name + "_" + mc.values.mkString("-"))
              .mkString("_")
            respondWithHeader(
              RawHeader("Content-Disposition",
                        s"""attachment; filename="${filename}.txt"""")) {
              extractFieldsForRawQueries { remainingQueryFields =>
                val allFields = mandatoryFields ++ remainingQueryFields
                log.info("Pipe: " + allFields)
                complete(
                  HttpEntity(`text/plain(UTF-8)`,
                             pipeSource(browserService.fetchData(allFields))))
              }
            }
          }
        }
    }
  }
}
