package hmda.api.http

import akka.actor.Props
import akka.actor.typed.{ ActorSystem, DispatcherSelector }
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.ConfigFactory
import hmda.api.http.filing.FilingHttpApi
import hmda.api.http.filing.InstitutionHttpApi
import hmda.api.http.routes.BaseHttpApi
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import akka.actor.typed.scaladsl.adapter._
import hmda.api.http.filing.submissions._
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.keycloak.representations.adapters.config.AdapterConfig
import org.slf4j.LoggerFactory

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

object HmdaFilingApi {
  def props: Props        = Props(new HmdaFilingApi)
  final val filingApiName = "hmda-filing-api"
}

class HmdaFilingApi
  extends HttpServer
    with BaseHttpApi
    with FilingHttpApi
    with SubmissionHttpApi
    with UploadHttpApi
    with ParseErrorHttpApi
    with InstitutionHttpApi
    with VerifyHttpApi
    with SignHttpApi
    with EditsHttpApi {
  import HmdaFilingApi._

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem[_]     = context.system
  override implicit val materializer: Materializer = Materializer(system)
  implicit lazy val ec: ExecutionContext           = system.dispatchers.lookup("akka.blocking-upload-dispatcher")
  val timeout: Timeout                             = Timeout(config.getInt("hmda.http.timeout").seconds)
  override val log                                 = Logging(system, getClass)

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

  val sharding = ClusterSharding(system)

  override val name: String = filingApiName
  override val host: String = config.getString("hmda.http.filingHost")
  override val port: Int    = config.getInt("hmda.http.filingPort")

  override val paths: Route = routes(s"$name") ~
    filingRoutes(oAuth2Authorization) ~
    submissionRoutes(oAuth2Authorization) ~
    uploadRoutes(oAuth2Authorization) ~
    institutionRoutes(oAuth2Authorization) ~
    parserErrorRoute(oAuth2Authorization) ~
    verifyRoutes(oAuth2Authorization) ~
    signRoutes(oAuth2Authorization) ~
    editsRoutes(oAuth2Authorization)

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}