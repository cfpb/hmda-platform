package hmda.api.http

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.{CoordinatedShutdown, ActorSystem => ClassicActorSystem}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import akka.util.Timeout
import hmda.api.http.admin.{InstitutionAdminHttpApi, PublishAdminHttpApi, StatsAdminHttpApi, SubmissionAdminHttpApi}
import hmda.api.http.directives.HmdaTimeDirectives._
import hmda.api.http.routes.BaseHttpApi
import hmda.auth.OAuth2Authorization
import org.slf4j.Logger

import scala.compat.java8.DurationConverters.DurationOps
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// This is just a Guardian for starting up the API
// $COVERAGE-OFF$
object HmdaAdminApi {
  val name = "hmda-admin-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[_]      = ctx.system
    implicit val classic: ClassicActorSystem = system.toClassic
    implicit val mat: Materializer           = Materializer(ctx)
    implicit val ec: ExecutionContext        = ctx.executionContext
    val log: Logger                          = ctx.log
    val sharding                             = ClusterSharding(system)
    val config                               = system.settings.config
    implicit val timeout: Timeout            = Timeout(config.getInt("hmda.http.timeout").seconds)
    val host: String                         = config.getString("hmda.http.adminHost")
    val port: Int                            = config.getInt("hmda.http.adminPort")
    val countTimeout: Duration               = config.getDuration("hmda.http.countTimeout").toScala
    val shutdown                             = CoordinatedShutdown(system)

    val oAuth2Authorization = OAuth2Authorization(log, config)
    val institutionRoutes   = InstitutionAdminHttpApi.create(config, sharding)
    val publishRoutes       = PublishAdminHttpApi.create(sharding, config)
    val submissionRoutes    = SubmissionAdminHttpApi.create(log, config, sharding, countTimeout)
    val statsRoutes         = StatsAdminHttpApi.create(log, config, sharding)
    val routes = BaseHttpApi.routes(name) ~
      institutionRoutes(oAuth2Authorization) ~
      publishRoutes(oAuth2Authorization) ~
      submissionRoutes(oAuth2Authorization) ~
      statsRoutes(oAuth2Authorization)

    BaseHttpApi.runServer(shutdown, name)(timed(routes), host, port)
    Behaviors.empty
  }
}
// $COVERAGE-ON$