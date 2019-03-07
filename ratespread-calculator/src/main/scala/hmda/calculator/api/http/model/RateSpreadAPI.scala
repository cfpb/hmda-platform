package hmda.calculator.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.HttpServer
import hmda.api.http.routes.BaseHttpApi
import scala.concurrent.{ExecutionContext, Future}

object RateSpreadAPI {
  def props(): Props = Props(new RateSpreadAPI)
}

class RateSpreadAPI extends HttpServer with BaseHttpApi with RateSpreadAPIRoutes {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val duration = config.getInt("hmda.uli.http.timeout").seconds

  override implicit val timeout: Timeout = Timeout(duration)

  override val name: String = "hmda-uli-api"
  override val host: String = config.getString("hmda.uli.http.host")
  override val port: Int = config.getInt("hmda.uli.http.port")

  override val paths: Route = routes(s"$name") ~ rateSpreadAPIRoutes

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
