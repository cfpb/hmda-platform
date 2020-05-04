package hmda.dataBrowser.api

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.Materializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.dataBrowser.api.DataBrowserDirectives._
import hmda.dataBrowser.models.HealthCheckStatus.Up
import hmda.dataBrowser.models._
import hmda.dataBrowser.services._
import monix.execution.Scheduler.Implicits.global
import org.slf4j.Logger

import scala.util.{ Failure, Success }

object DataBrowserHttpApi {
  def create(log: Logger, fileCache: S3FileService, query: QueryService, healthCheck: HealthCheckService)(
    implicit mat: Materializer
  ): Route =
    new DataBrowserHttpApi(log, fileCache, query, healthCheck).dataBrowserRoutes
}

private class DataBrowserHttpApi(log: Logger, fileCache: S3FileService, query: QueryService, healthCheck: HealthCheckService) {

  val Csv          = "csv"
  val Pipe         = "pipe"
  val Aggregations = "aggregations"

  def serveData(queries: List[QueryField], delimiter: Delimiter, errorMessage: String): Route =
    onComplete(obtainDataSource(fileCache, query)(queries, delimiter).runToFuture) {
      case Failure(ex) =>
        log.error(errorMessage, ex)
        complete(StatusCodes.InternalServerError)

      case Success(Left(byteSource)) =>
        complete(
          HttpEntity(`text/plain(UTF-8)`, byteSource)
        )

      case Success(Right(url)) =>
        redirect(Uri(url), StatusCodes.MovedPermanently)
    }

  val dataBrowserRoutes: Route =
    encodeResponse {
      pathPrefix("view") {
        pathPrefix("count") {
          extractCountFields { countFields =>
            log.info("Counts: " + countFields)
            complete(
              query
                .fetchAggregate(countFields)
                .map(aggs => AggregationResponse(Parameters.fromBrowserFields(countFields), aggs))
                .runToFuture
            )
          }
        } ~
          pathPrefix("nationwide") {
            extractFieldsForRawQueries { queryFields =>
              // GET /view/nationwide/csv
              (path(Csv) & get) {
                extractNationwideMandatoryYears { mandatoryFields =>
                  //remove filters that have all options selected
                  val allFields = (queryFields ++ mandatoryFields).filterNot(eachQueryField => eachQueryField.isAllSelected)
                  log.info("Nationwide [CSV]: " + allFields)
                  contentDispositionHeader(allFields, Commas) {
                    serveData(allFields, Commas, s"Failed to perform nationwide CSV query with the following queries: $allFields")
                  }
                }
              } ~
                // GET /view/nationwide/pipe
                (path(Pipe) & get) {
                  extractNationwideMandatoryYears { mandatoryFields =>
                    //remove filters that have all options selected
                    val allFields = (queryFields ++ mandatoryFields).filterNot(eachQueryField => eachQueryField.isAllSelected)
                    log.info("Nationwide [Pipe]: " + allFields)
                    contentDispositionHeader(allFields, Pipes) {
                      serveData(allFields, Pipes, s"Failed to perform nationwide PSV query with the following queries: $allFields")
                    }
                  }

                }
            } ~
              // GET /view/nationwide/aggregations
              (path(Aggregations) & get) {
                extractFieldsForAggregation { queryFields =>
                  val allFields = queryFields
                  log.info("Nationwide [Aggregations]: " + allFields)
                  complete(
                    query
                      .fetchAggregate(allFields)
                      .map(aggs => AggregationResponse(Parameters.fromBrowserFields(allFields), aggs))
                      .runToFuture
                  )
                }
              }
          } ~
          // GET /view/aggregations
          (path(Aggregations) & get) {
            extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields { mandatoryFields =>
              log.info("Aggregations: " + mandatoryFields)
              extractFieldsForAggregation { remainingQueryFields =>
                val allFields = mandatoryFields ++ remainingQueryFields

                complete(
                  query
                    .fetchAggregate(allFields)
                    .map(aggs => AggregationResponse(Parameters.fromBrowserFields(allFields), aggs))
                    .runToFuture
                )
              }
            }
          } ~
          // GET /view/csv
          (path(Csv) & get) {
            extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields { mandatoryFields =>
              extractFieldsForRawQueries { remainingQueryFields =>
                val allFields = mandatoryFields ++ remainingQueryFields
                log.info("CSV: " + allFields)
                contentDispositionHeader(allFields, Commas) {
                  serveData(allFields, Commas, s"Failed to fetch data for /view/csv with the following queries: $allFields")
                }
              }
            }
          } ~
          // GET /view/pipe
          (path(Pipe) & get) {
            extractYearsAndMsaAndStateAndCountyAndLEIBrowserFields { mandatoryFields =>
              extractFieldsForRawQueries { remainingQueryFields =>
                val allFields = mandatoryFields ++ remainingQueryFields
                log.info("PIPE: " + allFields)
                contentDispositionHeader(allFields, Pipes) {
                  serveData(allFields, Pipes, s"Failed to fetch data for /view/pipe with the following queries: $allFields")
                }
              }
            }
          } ~
          // GET /view/filers?years=2018&states=<csv-states> -- GET LEIs for specific states
          // GET /view/filers?years=2018&msamds=<csv-msamds> -- GET LEIs for specific msamds
          // GET /view/filers?years=2018&counties=<csv-counties> -- GET LEIs for specific counties
          // GET /view/filers?years=2018 -- GET all LEIs
          (path("filers") & get) {
            extractYearsMsaMdsStatesAndCounties { filerFields =>
              log.info("Filers: " + filerFields)
              onComplete(query.fetchFilers(filerFields).runToFuture) {
                case Failure(ex) =>
                  log.error("Failed to obtain filer information", ex)
                  complete(StatusCodes.InternalServerError)

                case Success(filerResponse) =>
                  complete(StatusCodes.OK, filerResponse)
              }
            }
          }
      } ~
        pathPrefix("health") {
          onComplete(healthCheck.healthCheckStatus.runToFuture) {
            case Success(HealthCheckResponse(Up, Up, Up)) =>
              complete(StatusCodes.OK)

            case Success(hs) =>
              log.warn(s"Service degraded cache=${hs.cache} db=${hs.db} s3=${hs.s3}")
              complete(StatusCodes.ServiceUnavailable)

            case Failure(ex) =>
              log.error("Failed to perform a health check", ex)
              complete(StatusCodes.InternalServerError)
          }
        }

    }
}