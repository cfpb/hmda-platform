package hmda.data.browser

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => UntypedActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import hmda.data.browser.repositories._
import hmda.data.browser.rest.Routes
import hmda.data.browser.services.{BrowserService, ModifiedLarBrowserService}
import io.lettuce.core.api.async.RedisAsyncCommands
import io.lettuce.core.{ClientOptions, RedisClient}
import monix.eval.Task
import monix.execution.{Scheduler => MonixScheduler}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.{Failure, Success}

object ApplicationGuardian {

  sealed trait Protocol
  private case class Ready(port: Int) extends Protocol
  private case class Error(errorMessage: String) extends Protocol

  def behavior: Behavior[Protocol] = Behaviors.setup { ctx =>
    implicit val untypedSystem: UntypedActorSystem =
      ctx.asScala.system.toUntyped
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val monixScheduler: MonixScheduler =
      MonixScheduler(ctx.executionContext)

    val settings = Settings(untypedSystem)

    val databaseConfig = DatabaseConfig.forConfig[JdbcProfile]("db")
    val repository: ModifiedLarRepository =
      new PostgresModifiedLarRepository(settings.database.tableName,
                                        databaseConfig)

    // We make the creation of the Redis client effectful because it can fail and we would like to operate
    // the service even if the cache is down (we provide fallbacks in case we receive connection errors)
    val redisClientTask: Task[RedisAsyncCommands[String, String]] = {
      val client = RedisClient.create(settings.redis.url)
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
      new RedisModifiedLarAggregateCache(redisClientTask, settings.redis.ttl)

    val service: BrowserService =
      new ModifiedLarBrowserService(repository, cache)

    val routes = Routes(service, settings)

    Http()
      .bindAndHandle(routes,
                     settings.server.host,
                     settings.server.port)
      .onComplete {
        case Success(value)     => ctx.self ! Ready(settings.server.port)
        case Failure(exception) => ctx.self ! Error(exception.getMessage)
      }

    Behaviors.receiveMessage {
      case Ready(port) =>
        ctx.log.info(s"Server is up and running on port $port")
        Behaviors.same

      case Error(errorMessage) =>
        ctx.log.error(s"Failed to start server because of $errorMessage")
        ctx.log.warning(s"Stopping Server Guardian: ${ctx.self.path}")
        Behaviors.stopped
    }
  }
}
