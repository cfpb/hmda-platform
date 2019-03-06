package hmda.reporting.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.pattern.pipe
import hmda.api.http.HttpServer
import hmda.api.http.routes.BaseHttpApi
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object HmdaReportingApi {
  def props(): Props = Props(new HmdaReportingApi)
}

class HmdaReportingApi
    extends HttpServer
    with BaseHttpApi
    with ReportingHttpApi {

  val config: Config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val duration: FiniteDuration =
    config.getInt("hmda.reporting.http.timeout").seconds

  override implicit val timeout: Timeout = Timeout(duration)

  override val name: String = "hmda-reporting-api"
  override val host: String = config.getString("hmda.reporting.http.host")
  override val port: Int = config.getInt("hmda.reporting.http.port")

  override val paths: Route = routes(s"$name") ~ hmdaFilerRoutes

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
