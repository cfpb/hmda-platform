package hmda.institution.api.http

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.{ ActorSystem, CoordinatedShutdown }
import akka.http.scaladsl.server.Directives._
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import hmda.auth.OAuth2Authorization

import scala.concurrent.ExecutionContext

// $COVERAGE-OFF$
object HmdaInstitutionQueryApi {
  val name = "hmda-institution-api"
  //  def props(): Props = Props(new HmdaInstitutionQueryApi)

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val ec: ExecutionContext = ctx.executionContext
    implicit val classic: ActorSystem = ctx.system.toClassic
    val shutdown: CoordinatedShutdown = CoordinatedShutdown(ctx.system)
    val config                        = classic.settings.config
    val log                           = ctx.log
    val oAuth2Authorization           = OAuth2Authorization(log, config)
    val host: String                  = config.getString("hmda.institution.http.host")
    val port: Int                     = config.getInt("hmda.institution.http.port")
    val proxyRoute                    = InstitutionQueryHttpApi.create(config)

    val routes = BaseHttpApi.routes(name) ~ proxyRoute(oAuth2Authorization)
    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)

    Behaviors.ignore
  }
}
// $COVERAGE-ON$