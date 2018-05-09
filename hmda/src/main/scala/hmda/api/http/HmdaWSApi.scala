package hmda.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.routes.BaseWsApi

import scala.concurrent.{ExecutionContext, Future}

object HmdaWSApi {
  def props: Props = Props(new HmdaWSApi)
}

class HmdaWSApi extends HttpServer with BaseWsApi {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  override val name: String = "hmda-ws-api"
  override val host: String = config.getString("hmda.ws.host")
  override val port: Int = config.getInt("hmda.ws.port")

  override val paths: Route = routes(s"$name")

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self
}
