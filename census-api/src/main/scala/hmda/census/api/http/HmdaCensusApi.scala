package hmda.census.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.pattern.pipe
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.HttpServer
import hmda.api.http.routes.BaseHttpApi
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout
import hmda.census.api.http.CensusHttpApi

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

object HmdaCensusApi {
  def props(): Props = Props(new HmdaCensusApi)
}

class HmdaCensusApi extends HttpServer with BaseHttpApi with CensusHttpApi {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val duration = config.getInt("hmda.census.http.timeout").seconds

  override implicit val timeout: Timeout = Timeout(duration)

  override val name: String = "hmda-census-api"
  override val host: String = config.getString("hmda.census.http.host")
  override val port: Int = config.getInt("hmda.census.http.port")

  override val paths: Route = routes(s"$name") ~ censusHttpRoutes

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
