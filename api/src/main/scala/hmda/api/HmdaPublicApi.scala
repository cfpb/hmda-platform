package hmda.api

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.BaseHttpApi

import scala.concurrent.{ExecutionContext, Future}

class HmdaPublicApi extends HttpApi with BaseHttpApi {

  val config = ConfigFactory.load()

  override val name: String = "hmda-public-api"

  override val host: String = config.getString("hmda.http.publicHost")
  override val port: Int = config.getInt("hmda.http.publicPort")

  override implicit val ec: ExecutionContext = context.dispatcher
  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()

  override val paths: Route = _
  override val http: Future[ServerBinding] = _
}
