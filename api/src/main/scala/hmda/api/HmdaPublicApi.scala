package hmda.api

import akka.actor.{ ActorSystem, Props }
import akka.event.Logging
import akka.pattern.pipe
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.BaseHttpApi
import hmda.api.http.public.PublicHttpApi

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object HmdaPublicApi {
  def props(): Props = Props(new HmdaPublicApi)
}

class HmdaPublicApi extends HttpApi with BaseHttpApi with PublicHttpApi {

  val config = ConfigFactory.load()

  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  override val name: String = "hmda-public-api"

  override val host: String = config.getString("hmda.http.publicHost")
  override val port: Int = config.getInt("hmda.http.publicPort")

  override implicit val ec: ExecutionContext = context.dispatcher
  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val log = Logging(system, getClass)

  override val paths: Route = routes(name) ~ publicHttpRoutes
  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
