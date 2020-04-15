package hmda.dataBrowser.api

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.dataBrowser.Settings
import hmda.dataBrowser.api.DataBrowserDirectives._
import hmda.dataBrowser.models.HealthCheckStatus.Up
import hmda.dataBrowser.models._
import hmda.dataBrowser.repositories._
import hmda.dataBrowser.services._
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.{ ClientOptions, RedisClient }
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.{ Failure, Success }

trait DataBrowserHttpApi extends Settings {

  val Csv          = "csv"
  val Pipe         = "pipe"
  val Aggregations = "aggregations"
  val log: LoggingAdapter
  implicit val materializer: ActorMaterializer

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("databrowser_db")
  val repository2018 =
    new PostgresModifiedLarRepository(database.tableName2018, databaseConfig)
  val repository2017 =
    new PostgresModifiedLarRepository2017(database.tableName2017, databaseConfig)

  // We make the creation of the Redis client effectful because it can fail and we would like to operate
  // the service even if the cache is down (we provide fallbacks in case we receive connection errors)
  val redisClientTask: Task[RedisAsyncCommands[String, String]] = {
    val client = RedisClient.create(redis.url)
    Task.eval {
      client.setOptions(
        ClientOptions
          .builder()
          .autoReconnect(true)
          .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
          .cancelCommandsOnReconnectFailure(true)
          .build()
      )

      client
        .connect()
        .async()
    }.memoizeOnSuccess
    // we memoizeOnSuccess because if we manage to create the client, we do not want to recompute it because the
    // client creation process is expensive and the client is able to recover internally when Redis comes back
  }

  val cache =
    new RedisCache(redisClientTask, redis.ttl)

  val query: QueryService =
    new DataBrowserQueryService(repository2018, repository2017, cache)

  val fileCache = new S3FileService

  val healthCheck: HealthCheckService =
    new HealthCheckService(repository2018, cache, fileCache)

  def serveData(queries: QueryFields, delimiter: Delimiter, errorMessage: String): Route =
    onComplete(obtainDataSource(fileCache, query)(queries, delimiter).runToFuture) {
      case Failure(ex) =>
        log.error(ex, errorMessage)
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
                .map(aggs => AggregationResponse(Parameters.fromBrowserFields(countFields.queryFields), aggs))
                .runToFuture
            )
          }
        } ~
          pathPrefix("nationwide") {
            // GET /view/nationwide/csv
            (path(Csv) & get) {
              extractNationwideMandatoryYears { mandatoryFields =>
                extractFieldsForRawQueries(mandatoryFields.year) { queryFields =>
                  //remove filters that have all options selected
                  val allFields = QueryFields(mandatoryFields.year, (queryFields.queryFields ++ mandatoryFields.queryFields).filterNot { eachQueryField =>
                    eachQueryField.isAllSelected
                  })
                  log.info("Nationwide [CSV]: " + allFields)
                  contentDispositionHeader(allFields.queryFields, Commas) {
                    serveData(allFields, Commas, s"Failed to perform nationwide CSV query with the following queries: $allFields")
                  }
                }
              }
            } ~
              // GET /view/nationwide/pipe
            (path(Pipe) & get) {
              extractNationwideMandatoryYears { mandatoryFields =>
                extractFieldsForRawQueries(mandatoryFields.year) { queryFields =>
                  //remove filters that have all options selected
                  val allFields = QueryFields(mandatoryFields.year, (queryFields.queryFields ++ mandatoryFields.queryFields).filterNot { eachQueryField =>
                    eachQueryField.isAllSelected
                  })
                  log.info("Nationwide [Pipe]: " + allFields)
                  contentDispositionHeader(allFields.queryFields, Pipes) {
                    serveData(allFields, Pipes, s"Failed to perform nationwide PSV query with the following queries: $allFields")
                  }
                }
              }

            } ~
              // GET /view/nationwide/aggregations
              (path(Aggregations) & get) {
                extractNationwideMandatoryYears { mandatoryFields =>
                  extractFieldsForAggregation(mandatoryFields.year) { queryFields =>
                    val allFields = queryFields
                    log.info("Nationwide [Aggregations]: " + allFields)
                    complete(
                      query
                        .fetchAggregate(allFields)
                        .map(aggs => AggregationResponse(Parameters.fromBrowserFields(allFields.queryFields), aggs))
                        .runToFuture
                    )
                  }
                }
              }
          } ~
          // GET /view/aggregations
          (path(Aggregations) & get) {
            extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields { mandatoryFields =>
              log.info("Aggregations: " + mandatoryFields)
              extractFieldsForAggregation(mandatoryFields.year) { remainingQueryFields =>
                val allFields = QueryFields(mandatoryFields.year, mandatoryFields.queryFields ++ remainingQueryFields.queryFields)

                complete(
                  query
                    .fetchAggregate(allFields)
                    .map(aggs => AggregationResponse(Parameters.fromBrowserFields(allFields.queryFields), aggs))
                    .runToFuture
                )
              }
            }
          } ~
          // GET /view/csv
          (path(Csv) & get) {
            extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields { mandatoryFields =>
              extractFieldsForRawQueries(mandatoryFields.year) { remainingQueryFields =>
                val allFields = QueryFields(mandatoryFields.year, mandatoryFields.queryFields ++ remainingQueryFields.queryFields)
                log.info("CSV: " + allFields)
                contentDispositionHeader(allFields.queryFields, Commas) {
                  serveData(allFields, Commas, s"Failed to fetch data for /view/csv with the following queries: ${allFields.queryFields}")
                }
              }
            }
          } ~
          // GET /view/pipe
          (path(Pipe) & get) {
            extractMsaAndStateAndCountyAndInstitutionIdentifierBrowserFields { mandatoryFields =>
              extractFieldsForRawQueries(mandatoryFields.year) { remainingQueryFields =>
                val allFields = QueryFields(mandatoryFields.year, mandatoryFields.queryFields ++ remainingQueryFields.queryFields)
                log.info("PIPE: " + allFields)
                contentDispositionHeader(allFields.queryFields, Pipes) {
                  serveData(allFields, Pipes, s"Failed to fetch data for /view/pipe with the following queries: ${allFields.queryFields}")
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
                  log.error(ex, "Failed to obtain filer information")
                  complete(StatusCodes.InternalServerError)

                case Success(filerResponse) =>
                  complete((StatusCodes.OK, filerResponse))
              }
            }
          }
      } ~
        pathPrefix("health") {
          onComplete(healthCheck.healthCheckStatus.runToFuture) {
            case Success(HealthCheckResponse(Up, Up, Up)) =>
              complete(StatusCodes.OK)

            case Success(hs) =>
              log.warning(s"Service degraded cache=${hs.cache} db=${hs.db} s3=${hs.s3}")
              complete(StatusCodes.ServiceUnavailable)

            case Failure(ex) =>
              log.error(ex, "Failed to perform a health check")
              complete(StatusCodes.InternalServerError)
          }
        }

    }
}
