package hmda.api

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http._
import hmda.persistence.HmdaSupervisor
import hmda.persistence.HmdaSupervisor.FindActorByName
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.processing.{ LocalHmdaEventProcessor, SingleLarValidation }

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

  val supervisor = HmdaSupervisor.createSupervisor(system)

  (supervisor ? FindActorByName(SingleLarValidation.name))
    .mapTo[ActorRef]
    .map { actor =>
      log.info(s"Started validator at ${actor.path}")
    }

  (supervisor ? FindActorByName(LocalHmdaEventProcessor.name))
    .mapTo[ActorRef]
    .map { actor =>
      log.info(s"Started event processor at ${actor.path}")
    }

  (supervisor ? FindActorByName(InstitutionPersistence.name))
    .mapTo[ActorRef]
    .map { actor =>
      log.info(s"Started institutions at ${actor.path}")
    }

  val http = Http().bindAndHandle(
    routes ~ larRoutes ~ institutionsRoutes,
    host,
    port
  )

  //Load demo data
  lazy val isDemo = config.getBoolean("hmda.isDemo")
  if (isDemo) {
    DemoData.loadDemoData(system)
  }

  http onFailure {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}", host, port)
  }

}
