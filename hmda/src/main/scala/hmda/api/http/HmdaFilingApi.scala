package hmda.api.http

import akka.Done
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ ActorSystem, Behavior }
import akka.actor.{ CoordinatedShutdown, ActorSystem => ClassicActorSystem }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.util.Timeout
import hmda.api.http.filing.submissions._
import hmda.api.http.filing.{ FilingHttpApi, InstitutionHttpApi }
import hmda.api.http.routes.BaseHttpApi
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.representations.adapters.config.AdapterConfig

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object HmdaFilingApi {
  val name = "hmda-filing-api"

  def apply(): Behavior[Nothing] = Behaviors.setup[Nothing] { ctx =>
    implicit val system: ActorSystem[_]      = ctx.system
    implicit val classic: ClassicActorSystem = system.toClassic
    implicit val mat: Materializer           = Materializer(system)
    implicit val ec: ExecutionContext        = ctx.executionContext
    val config                               = system.settings.config
    val shutdown                             = CoordinatedShutdown(classic)
    implicit val timeout: Timeout            = Timeout(config.getInt("hmda.http.timeout").seconds)
    val log                                  = ctx.log
    val sharding                             = ClusterSharding(system)
    val filingApiName                        = name
    val authUrl                              = config.getString("keycloak.auth.server.url")
    val keycloakRealm                        = config.getString("keycloak.realm")
    val apiClientId                          = config.getString("keycloak.client.id")
    val host: String                         = config.getString("hmda.http.filingHost")
    val port: Int                            = config.getInt("hmda.http.filingPort")

    val oAuth2Authorization = {
      val adapterConfig = new AdapterConfig()
      adapterConfig.setRealm(keycloakRealm)
      adapterConfig.setAuthServerUrl(authUrl)
      adapterConfig.setResource(apiClientId)
      val keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig)
      OAuth2Authorization(log, new KeycloakTokenVerifier(keycloakDeployment))
    }

    val base              = BaseHttpApi.routes(filingApiName)
    val filingRoutes      = FilingHttpApi.create(log, sharding)
    val submissionRoutes  = SubmissionHttpApi.create(log, sharding)
    val uploadRoutes      = UploadHttpApi.create(log, sharding)
    val institutionRoutes = InstitutionHttpApi.create(log, sharding)
    val parserErrorRoutes = ParseErrorHttpApi.create(log, sharding)
    val verifyRoutes      = VerifyHttpApi.create(log, sharding)
    val signRoutes        = SignHttpApi.create(log, sharding)
    val editRoutes        = EditsHttpApi.create(log, sharding)

    val httpRoutes: Route =
      base ~
        List(filingRoutes, submissionRoutes, uploadRoutes, institutionRoutes, parserErrorRoutes, verifyRoutes, signRoutes, editRoutes)
          .map(fn => fn(oAuth2Authorization))
          .reduce((r1, r2) => r1 ~ r2)

    Http().bindAndHandle(httpRoutes, host, port).onComplete {
      case Failure(exception) =>
        system.log.error("Failed to start HTTP server, shutting down", exception)
        system.terminate()

      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "HTTP Server online at http://{}:{}/",
          address.getHostString,
          address.getPort
        )

        shutdown.addTask(
          CoordinatedShutdown.PhaseServiceRequestsDone,
          "http-walletserver-graceful-terminate"
        ) { () =>
          binding.terminate(10.seconds).map { _ =>
            system.log.info(
              "HTTP Server http://{}:{}/ graceful shutdown completed",
              address.getHostString,
              address.getPort
            )
            Done
          }
        }
    }

    Behaviors.empty
  }
}