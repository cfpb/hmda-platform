package hmda.api.http

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.{CoordinatedShutdown, ActorSystem => ClassicActorSystem}
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import hmda.api.http.public.{HmdaFileParsingHttpApi, HmdaFileValidationHttpApi, LarValidationHttpApi, TsValidationHttpApi}
import hmda.api.http.routes.BaseHttpApi
import hmda.api.http.directives.HmdaTimeDirectives._

import scala.concurrent.ExecutionContext
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.adapter._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors

// This is just a Guardian for starting up the API
// $COVERAGE-OFF$
object HmdaPublicApi {
  val name = "hmda-public-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    val system: ActorSystem[_]               = ctx.system
    implicit val classic: ClassicActorSystem = system.toClassic
    val shutdown                             = CoordinatedShutdown(system)
    implicit val ec: ExecutionContext        = ctx.executionContext
    implicit val mat: Materializer           = Materializer(ctx)
    val config                               = system.settings.config
    val host: String                         = config.getString("hmda.http.publicHost")
    val port: Int                            = config.getInt("hmda.http.publicPort")
    val routes                               = cors() {
      BaseHttpApi.routes(name) ~ TsValidationHttpApi.create ~ LarValidationHttpApi.create ~ HmdaFileValidationHttpApi.create ~ HmdaFileParsingHttpApi.create
    }


    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)

    Behaviors.empty
  }
}
// $COVERAGE-ON$