package hmda.api

import java.net.InetSocketAddress

import akka.actor.{ Actor, ActorRef, ActorSystem, Props, Status }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.pattern.{ ask, pipe }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.{ HmdaCustomDirectives, HttpApi, InstitutionsHttpApi, LarHttpApi }
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.processing.{ LocalHmdaEventProcessor, SingleLarValidation }

import scala.concurrent.Future
import scala.concurrent.duration._

object HmdaApi {
  case object StartHttpApi
  def props(): Props = Props(new HmdaApi)
}

class HmdaApi
    extends Actor
    with HttpApi
    with LarHttpApi
    with InstitutionsHttpApi
    with HmdaCustomDirectives {

  import HmdaApi._

  val config = ConfigFactory.load()
  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  lazy val host = config.getString("hmda.http.host")
  lazy val port = config.getInt("hmda.http.port")

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val log = Logging(system, getClass)
  implicit val ec = context.dispatcher

  val http: Future[ServerBinding] = Http(system).bindAndHandle(
    routes ~ larRoutes ~ institutionsRoutes,
    host,
    port
  )

  http pipeTo self

  override def receive: Receive = {
    case Http.ServerBinding(s) => handleServerBinding(s)
    case Status.Failure(e) => handleBindFailure(e)
    case StartHttpApi => handleHttpApiStartup()

  }

  private def handleServerBinding(address: InetSocketAddress) = {
    log.info(s"HMDA API started on {}", address)
    context.become(Actor.emptyBehavior)
  }

  private def handleBindFailure(error: Throwable) = {
    log.error(error, s"Failed to bind to $host:$port")
    context stop self
  }

  private def handleHttpApiStartup() = {

    log.info("Start API")

    val supervisor = system.actorSelection("/user/supervisor")

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

    //Load demo data
    lazy val isDemo = config.getBoolean("hmda.isDemo")
    if (isDemo) {
      DemoData.loadDemoData(system)
    }
  }

}
