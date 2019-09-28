//package hmda.api.http
//
//import akka.actor.{ ActorSystem, Props }
//import akka.cluster.sharding.typed.scaladsl.ClusterSharding
//import akka.event.Logging
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Route
//import akka.pattern.pipe
//import akka.stream.ActorMaterializer
//import akka.util.Timeout
//import hmda.api.http.admin.InstitutionAdminHttpApi
//import hmda.api.http.routes.BaseHttpApi
//import akka.http.scaladsl.server.Directives._
//import akka.actor.typed.scaladsl.adapter._
//import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
//import org.keycloak.adapters.KeycloakDeploymentBuilder
//import org.keycloak.representations.adapters.config.AdapterConfig
//
//import scala.concurrent.{ ExecutionContext, Future }
//import scala.concurrent.duration._
//
//object HmdaAdminApi {
//  def props: Props       = Props(new HmdaAdminApi)
//  final val adminApiName = "hmda-admin-api"
//}
//
//class HmdaAdminApi extends HttpServer with BaseHttpApi with InstitutionAdminHttpApi {
//  import HmdaAdminApi._
//
//  override implicit val system: ActorSystem             = context.system
//  override implicit val materializer: ActorMaterializer = ActorMaterializer()
//  override implicit val ec: ExecutionContext            = context.dispatcher
//  override val log                                      = Logging(system, getClass)
//  override val timeout: Timeout                         = Timeout(config.getInt("hmda.http.timeout").seconds)
//
//  val authUrl       = config.getString("keycloak.auth.server.url")
//  val keycloakRealm = config.getString("keycloak.realm")
//  val apiClientId   = config.getString("keycloak.client.id")
//
//  val adapterConfig = new AdapterConfig()
//  adapterConfig.setRealm(keycloakRealm)
//  adapterConfig.setAuthServerUrl(authUrl)
//  adapterConfig.setResource(apiClientId)
//  println(adapterConfig.getClientKeystore)
//  val keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig)
//
//  val oAuth2Authorization = OAuth2Authorization(
//    log,
//    new KeycloakTokenVerifier(keycloakDeployment)
//  )
//
//  override val sharding = ClusterSharding(system.toTyped)
//
//  override val name: String = adminApiName
//  override val host: String = config.getString("hmda.http.adminHost")
//  override val port: Int    = config.getInt("hmda.http.adminPort")
//
//  override val paths: Route = routes(s"$name") ~ institutionAdminRoutes(oAuth2Authorization)
//
//  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
//    paths,
//    host,
//    port
//  )
//
//  http pipeTo self
//}