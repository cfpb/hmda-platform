package hmda.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import hmda.api.http.{ HttpApi, LarHttpApi }
import hmda.api.processing.lar.SingleLarValidation

object HmdaApi extends App with HttpApi with LarHttpApi {

  override implicit val system = ActorSystem("hmda")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher

  override val log = Logging(system, getClass)
  val config = ConfigFactory.load()

  lazy val host = config.getString("hmda.http.host")
  lazy val port = config.getInt("hmda.http.port")

  //Start up API Actors
  import hmda.api.processing.lar.SingleLarValidation._
  val larValidation = createSingleLarValidator(system)

  val http = Http().bindAndHandle(
    routes ~ larRoutes,
    host,
    port
  )

  http onFailure {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}", host, port)
  }

}
