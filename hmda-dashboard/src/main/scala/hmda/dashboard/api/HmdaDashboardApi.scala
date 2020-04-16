package hmda.dashboard.api

import akka.actor.{ActorSystem, Props}
import akka.pattern.pipe
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import hmda.api.http.HttpServer
import hmda.api.http.routes.BaseHttpApi
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import hmda.auth.{KeycloakTokenVerifier, OAuth2Authorization}
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.representations.adapters.config.AdapterConfig

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object HmdaDashboardApi {
  def props(): Props = Props(new HmdaDashboardApi)
}

class HmdaDashboardApi
    extends HttpServer
    with BaseHttpApi
    with HmdaDashboardHttpApi {

    override implicit lazy val system: ActorSystem = context.system
    override implicit lazy val materializer: ActorMaterializer =
      ActorMaterializer()
    override implicit val ec: ExecutionContext = context.dispatcher
    override val log = Logging(system, getClass)

    val duration: FiniteDuration = server.askTimeout

    implicit val timeout: Timeout = Timeout(duration)

    override val name: String = "hmda-dashbaord"
    override val host: String = server.host
    override val port: Int = server.port

    val authUrl       = config.getString("keycloak.auth.server.url")
    val keycloakRealm = config.getString("keycloak.realm")
    val apiClientId   = config.getString("keycloak.client.id")

    val adapterConfig = new AdapterConfig()
    adapterConfig.setRealm(keycloakRealm)
    adapterConfig.setAuthServerUrl(authUrl)
    adapterConfig.setResource(apiClientId)
    println(adapterConfig.getClientKeystore)
    val keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig)

    val oAuth2Authorization = OAuth2Authorization(
      log,
      new KeycloakTokenVerifier(keycloakDeployment)
    )

    override val paths: Route = routes(s"$name") ~ hmdaDashboardRoutes(oAuth2Authorization)

    override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
      paths,
      host,
      port
    )

    http pipeTo self
}

