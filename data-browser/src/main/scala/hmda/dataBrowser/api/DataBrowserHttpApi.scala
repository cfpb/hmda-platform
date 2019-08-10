package hmda.dataBrowser.api

import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes.Found
import akka.http.scaladsl.model.{HttpEntity, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.dataBrowser.models._
import hmda.dataBrowser.api.DataBrowserDirectives._
import hmda.dataBrowser.services.ModifiedLarBrowserService
import io.circe.generic.auto._
import monix.execution.Scheduler.Implicits.global
import akka.event.LoggingAdapter
import hmda.dataBrowser.repositories.RedisModifiedLarAggregateCache
import hmda.dataBrowser.repositories.ModifiedLarAggregateCache
import hmda.dataBrowser.repositories.ModifiedLarRepository
import hmda.dataBrowser.repositories.PostgresModifiedLarRepository
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.{ClientOptions, RedisClient}
import hmda.dataBrowser.Settings
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import hmda.dataBrowser.services.BrowserService

trait DataBrowserHttpApi extends Settings {

  val Csv = "csv"
  val Pipe = "pipe"
  val Aggregations = "aggregations"
  val log: LoggingAdapter

  val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
  val repository: ModifiedLarRepository =
    new PostgresModifiedLarRepository(database.tableName, databaseConfig)

  // We make the creation of the Redis client effectful because it can fail and we would like to operate
  // the service even if the cache is down (we provide fallbacks in case we receive connection errors)
  val redisClientTask: Task[RedisAsyncCommands[String, String]] = {
    val client = RedisClient.create(redis.url)
    Task.eval {
      client.setOptions(
        ClientOptions
          .builder()
          .autoReconnect(true)
          .disconnectedBehavior(
            ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
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

  val cache: ModifiedLarAggregateCache =
    new RedisModifiedLarAggregateCache(redisClientTask, redis.ttl)

  val service: BrowserService =
    new ModifiedLarBrowserService(repository, cache)

  val dataBrowserRoutes =
    encodeResponse {
      pathPrefix("view") {
        pathPrefix("count") {
          extractCountFields { countFields =>
            log.info("Counts: " + countFields)
            complete(
              service
                .fetchAggregate(countFields)
                .map(aggs =>
                  AggregationResponse(Parameters.fromBrowserFields(countFields),
                    aggs))
                .runToFuture
            )
          }
        } ~
          pathPrefix("nationwide") {
            extractFieldsForRawQueries { queryFields =>
              // GET /view/nationwide/csv
              contentDisposition(queryFields) {
                (path(Csv) & get) {
                  extractNationwideMandatoryYears { mandatoryFields =>
                    //remove filters that have all options selected
                    val allFields = (queryFields ++ mandatoryFields).filterNot {
                      eachQueryField =>
                        eachQueryField.isAllSelected
                    }
                    log.info("Nationwide [CSV]: " + allFields)
                    if (allFields.size == 1 && allFields.head.name == "year") {
                      redirect(Uri(s3.nationwideCsv), Found)
                    } else
                      complete(
                        HttpEntity(
                          `text/plain(UTF-8)`,
                          csvSource(service.fetchData(queryFields))
                        )
                      )
                  }

                }
              } ~
                // GET /view/nationwide/pipe
                (path(Pipe) & get) {
                  extractNationwideMandatoryYears { mandatoryFields =>
                    //remove filters that have all options selected
                    val allFields = (queryFields ++ mandatoryFields).filterNot {
                      eachQueryField =>
                        eachQueryField.isAllSelected
                    }
                    log.info("Nationwide [Pipe]: " + allFields)
                    contentDisposition(queryFields) {
                      if (allFields.size == 1 && allFields.head.name == "year")
                        redirect(Uri(s3.nationwidePipe), Found)
                      else
                        complete(
                          HttpEntity(
                            `text/plain(UTF-8)`,
                            pipeSource(service.fetchData(queryFields))
                          )
                        )
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
                    service
                      .fetchAggregate(allFields)
                      .map(aggs =>
                        AggregationResponse(
                          Parameters.fromBrowserFields(allFields),
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
                log.info("Aggregations: " + allFields)
                complete(
                  service
                    .fetchAggregate(allFields)
                    .map(aggs =>
                      AggregationResponse(
                        Parameters.fromBrowserFields(allFields),
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
                val allFields = mandatoryFields ++ remainingQueryFields
                log.info("CSV: " + allFields)
                contentDisposition(allFields) {
                  complete(HttpEntity(`text/plain(UTF-8)`,
                    csvSource(service.fetchData(allFields))))
                }
              }
            }
          } ~
          // GET /view/pipe
          (path(Pipe) & get) {
            extractYearsAndMsaAndStateBrowserFields { mandatoryFields =>
              extractFieldsForRawQueries { remainingQueryFields =>
                val allFields = mandatoryFields ++ remainingQueryFields
                log.info("CSV: " + allFields)
                contentDisposition(allFields) {
                  complete(HttpEntity(`text/plain(UTF-8)`,
                    pipeSource(service.fetchData(allFields))))
                }
              }
            }
          }
      }
    }

}