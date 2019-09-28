//package hmda.api.http
//
//import akka.actor.{ ActorSystem, Props }
//import akka.event.Logging
//import akka.http.scaladsl.Http
//import akka.http.scaladsl.server.Route
//import akka.pattern.pipe
//import akka.stream.ActorMaterializer
//import com.typesafe.config.ConfigFactory
//import hmda.api.http.public.{ HmdaFileValidationHttpApi, LarValidationHttpApi, TsValidationHttpApi }
//import akka.http.scaladsl.server.Directives._
//import akka.util.Timeout
//import hmda.api.http.routes.BaseHttpApi._
//import hmda.api.http.directives.HmdaTimeDirectives._
//import org.slf4j.LoggerFactory
//
//import scala.concurrent.duration._
//import scala.concurrent.{ ExecutionContext, Future }
//
//object HmdaPublicApi {
//  def props: Props        = Props(new HmdaPublicApi)
//  final val publicApiName = "hmda-public-api"
//}
//
//class HmdaPublicApi extends HttpServer with TsValidationHttpApi with LarValidationHttpApi with HmdaFileValidationHttpApi {
//
//  import HmdaPublicApi._
//
//  val config = ConfigFactory.load()
//
//  override implicit val system: ActorSystem             = context.system
//  override implicit val materializer: ActorMaterializer = ActorMaterializer()
//  override implicit val ec: ExecutionContext            = context.dispatcher
//  override val log                                      = LoggerFactory.getLogger(getClass)
//
//  override val name: String     = publicApiName
//  override val host: String     = config.getString("hmda.http.publicHost")
//  override val port: Int        = config.getInt("hmda.http.publicPort")
//  override val timeout: Timeout = Timeout(config.getInt("hmda.http.timeout").seconds)
//
//  override val paths: Route = timed(routes(s"$name") ~ tsRoutes ~ larRoutes ~ hmdaFileRoutes)
//
//  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
//    paths,
//    host,
//    port
//  )
//
//  http pipeTo self
//}