package hmda.api.http

import org.apache.pekko.actor.typed.{ActorSystem, Behavior}
import org.apache.pekko.actor.{CoordinatedShutdown, ActorSystem => ClassicActorSystem}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.stream.Materializer
import hmda.api.http.public.{HmdaFileParsingHttpApi, HmdaFileValidationHttpApi, LarValidationHttpApi, TsValidationHttpApi}
import hmda.api.http.routes.BaseHttpApi
import hmda.api.http.directives.HmdaTimeDirectives._

import scala.concurrent.ExecutionContext
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.actor.typed.scaladsl.adapter._
import ch.megard.pekko.http.cors.scaladsl.CorsDirectives.cors

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