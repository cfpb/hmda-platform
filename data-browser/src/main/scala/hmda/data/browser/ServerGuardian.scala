package hmda.data.browser

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl._
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ActorSystem => UntypedActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import hmda.data.browser.repositories.{
  ModifiedLarRepository,
  PostgresModifiedLarRepository
}
import hmda.data.browser.rest.Routes
import hmda.data.browser.services.{BrowserService, ModifiedLarBrowserService}
import monix.execution.{Scheduler => MonixScheduler}
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.util.{Failure, Success}

object ServerGuardian {
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
      new PostgresModifiedLarRepository("modifiedlar2018", databaseConfig)
    val service: BrowserService = new ModifiedLarBrowserService(repository)

    Http()
      .bindAndHandle(Routes(service),
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