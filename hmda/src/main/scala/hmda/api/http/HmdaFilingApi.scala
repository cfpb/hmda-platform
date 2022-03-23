package hmda.api.http

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.{ CoordinatedShutdown, ActorSystem => ClassicActorSystem }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import hmda.api.http.filing.submissions._
import hmda.api.http.filing.{ FilingHttpApi, InstitutionHttpApi }
import hmda.api.http.routes.BaseHttpApi
import hmda.auth.OAuth2Authorization
import hmda.api.http.directives.HmdaTimeDirectives._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

// This is just a Guardian for starting up the API
// $COVERAGE-OFF$
object HmdaFilingApi {
  val name = "hmda-filing-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[_]      = ctx.system
    implicit val classic: ClassicActorSystem = system.toClassic
    implicit val mat: Materializer           = Materializer(ctx)
    implicit val ec: ExecutionContext        = ctx.executionContext
    val config                               = system.settings.config
    val shutdown                             = CoordinatedShutdown(system)
    implicit val timeout: Timeout            = Timeout(config.getInt("hmda.http.timeout").seconds)
    val log                                  = ctx.log
    val sharding                             = ClusterSharding(system)
    val filingApiName                        = name
    val host: String                         = config.getString("hmda.http.filingHost")
    val port: Int                            = config.getInt("hmda.http.filingPort")

    val oAuth2Authorization = OAuth2Authorization(log, config)
    val base                = BaseHttpApi.routes(filingApiName)
    val filingRoutes        = FilingHttpApi.create(log, sharding)
    val submissionRoutes    = SubmissionHttpApi.create(config, log, sharding)
    val uploadRoutes        = UploadHttpApi.create(log, sharding)
    val institutionRoutes   = InstitutionHttpApi.create(log, sharding)
    val parserErrorRoutes   = ParseErrorHttpApi.create(log, sharding)
    val verifyRoutes        = VerifyHttpApi.create(log, sharding)
    val signRoutes          = SignHttpApi.create(log, sharding)
    val editRoutes          = EditsHttpApi.create(log, sharding)

    val httpRoutes: Route =
      base ~
        List(filingRoutes, submissionRoutes, uploadRoutes, institutionRoutes, parserErrorRoutes, verifyRoutes, signRoutes, editRoutes)
          .map(fn => fn(oAuth2Authorization))
          .reduce((r1, r2) => r1 ~ r2)

    BaseHttpApi.runServer(shutdown, name)(timed(httpRoutes), host, port)

    Behaviors.empty
  }
}
// $COVERAGE-ON$