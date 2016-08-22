package hmda.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http._
import hmda.api.processing.lar.SingleLarValidation._
import hmda.persistence.institutions.InstitutionPersistence._
import hmda.persistence.demo.DemoData
import hmda.api.processing.LocalHmdaEventProcessor._
import scala.concurrent.duration._

object HmdaApi
    extends App
    with HttpApi
    with LarHttpApi
    with InstitutionsHttpApi
    with HmdaCustomDirectives {

  override implicit val system = ActorSystem("hmda")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher

  override val log = Logging(system, getClass)
  val config = ConfigFactory.load()

  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  lazy val host = config.getString("hmda.http.host")
  lazy val port = config.getInt("hmda.http.port")

  //Start up API Actors

  createSingleLarValidator(system)
  createInstitutions(system)
  createLocalHmdaEventProcessor(system)

  val http = Http().bindAndHandle(
    routes ~ larRoutes ~ institutionsRoutes,
    host,
    port
  )

  //Load demo data
  lazy val isDemo = config.getBoolean("hmda.isDemo")
  if (isDemo) {
    DemoData.loadData(system)
  }

  http onFailure {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}", host, port)
  }

}
