package hmda.dataBrowser.api

import akka.actor
import akka.actor.CoordinatedShutdown
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorSystem, Behavior, SupervisorStrategy }
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import akka.stream.Materializer
import hmda.dataBrowser.Settings
import hmda.dataBrowser.repositories.{ PostgresModifiedLarRepository, PostgresModifiedLarRepository2017, RedisModifiedLarAggregateCache }
import hmda.dataBrowser.services.{ DataBrowserQueryService, HealthCheckService, QueryService, S3FileService }
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.{ ClientOptions, RedisClient }
import monix.eval.Task
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// $COVERAGE-OFF$
// Application Guardian
object DataBrowserApi extends Settings {
  val name: String = "hmda-data-browser"

  def apply(): Behavior[Nothing] =
    Behaviors
      .supervise[Nothing] {
        Behaviors.setup[Nothing] { ctx =>
          implicit val system: ActorSystem[Nothing] = ctx.system
          implicit val classic: actor.ActorSystem   = system.toClassic
          implicit val mat: Materializer            = Materializer(ctx)
          implicit val ec: ExecutionContext         = ctx.executionContext
          val shutdown                              = CoordinatedShutdown(system)
          val log                                   = ctx.log
          val host: String                          = server.host
          val port: Int                             = server.port

          val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("databrowser_db")

          val repository2017 = new PostgresModifiedLarRepository2017(database.tableName2017, databaseConfig)

          val repositoryLatest = new PostgresModifiedLarRepository(databaseConfig, database.tableSelector)

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

          val cache = new RedisModifiedLarAggregateCache(redisClientTask, log, redis.ttl)

          val query: QueryService = new DataBrowserQueryService(repositoryLatest, repository2017, cache, log)

          val fileCache = new S3FileService

          val healthCheck: HealthCheckService = new HealthCheckService(repositoryLatest, cache, fileCache)

          val routes = cors() {
            BaseHttpApi.routes(name) ~ DataBrowserHttpApi.create(log, fileCache, query, healthCheck)
          }
          BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
          Behaviors.ignore
        }
      }
      .onFailure(SupervisorStrategy.restartWithBackoff(1.second, 30.seconds, 0.01))
}
// $COVERAGE-OFF$