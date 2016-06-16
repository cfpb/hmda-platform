package hmda.api

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.{ FilingsHttpApi, HttpApi, InstitutionsHttpApi, LarHttpApi }
import hmda.api.persistence.InstitutionPersistence._
import hmda.api.processing.lar.SingleLarValidation._
import hmda.api.demo.DemoData

import scala.concurrent.duration._

object HmdaApi
    extends App
    with HttpApi
    with LarHttpApi
    with InstitutionsHttpApi
    with FilingsHttpApi {

  override implicit val system = ActorSystem("hmda")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher

  override implicit val timeout = Timeout(30.seconds)

  override val log = Logging(system, getClass)
  val config = ConfigFactory.load()

  lazy val host = config.getString("hmda.http.host")
  lazy val port = config.getInt("hmda.http.port")

  //Start up API Actors

  createSingleLarValidator(system)
  createInstitutionsFiling(system)

  val http = Http().bindAndHandle(
    routes ~ larRoutes ~ institutionsRoutes ~ filingsRoutes,
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
