package hmda.calculator.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.HttpServer
import hmda.api.http.routes.BaseHttpApi

import scala.concurrent.{ExecutionContext, Future}

object RateSpreadAPI {
  def props(): Props = Props(new RateSpreadAPI)
}

class RateSpreadAPI
    extends HttpServer
    with BaseHttpApi
    with RateSpreadAPIRoutes {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  override val name: String = "hmda-ratespread-api"
  override val host: String = config.getString("hmda.ratespread.http.host")
  override val port: Int = config.getInt("hmda.ratespread.http.port")

  override val paths: Route = routes(s"$name") ~ rateSpreadRoutes

  override val http: Future[Http.ServerBinding] =
    Http(system).bindAndHandle(paths, host, port)

  http pipeTo self
}
