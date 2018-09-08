package hmda.api.http

import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.institutions.UploadApi
import hmda.api.http.routes.BaseHttpApi

import scala.concurrent.{ExecutionContext, Future}

object HmdaFilingApi {
  def props: Props = Props(new HmdaFilingApi)
  final val filingApiName = "hmda-filing-api"
}

class HmdaFilingApi extends HttpServer with BaseHttpApi with UploadApi {
  import HmdaFilingApi._

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  override val name: String = filingApiName
  override val host: String = config.getString("hmda.http.filingHost")
  override val port: Int = config.getInt("hmda.http.filingPort")

  override val paths: Route = routes(s"$name") ~ uploadRoutes

  override val http: Future[Http.ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
