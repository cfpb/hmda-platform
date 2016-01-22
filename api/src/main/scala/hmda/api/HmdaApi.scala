package hmda.api

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import hmda.api.http.HttpService
import hmda.api.protocol.HmdaApiProtocol
import com.typesafe.config.ConfigFactory

object HmdaApi extends App with HttpService {

  override implicit val system = ActorSystem("hmda-api")
  override implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  override val log = Logging(system, getClass)
  override val config = ConfigFactory.load()

  lazy val host = config.getString("hmda.http.host")
  lazy val port = config.getInt("hmda.http.port")

  val http = Http().bindAndHandle(
    routes,
    host,
    port
  )

  http onFailure {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}", host, port)
  }

}
