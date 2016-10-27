package hmda.api

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.{ ask, pipe }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.{ BaseHttpApi, HmdaCustomDirectives, InstitutionsHttpApi, LarHttpApi }
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.processing.{ LocalHmdaEventProcessor, SingleLarValidation }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object HmdaApi {
  case object StartHttpApi
  def props(): Props = Props(new HmdaApi)
}

class HmdaApi
    extends HttpApi
    with BaseHttpApi
    with LarHttpApi
    with InstitutionsHttpApi
    with HmdaCustomDirectives {

  import HmdaApi._

  val config = ConfigFactory.load()
  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  implicit val timeout = Timeout(httpTimeout.seconds)

  override lazy val host = config.getString("hmda.http.host")
  override lazy val port = config.getInt("hmda.http.port")

  implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val paths: Route = routes ~ larRoutes ~ institutionsRoutes

  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

  override def receive: Receive = super.receive orElse {
    case StartHttpApi => handleHttpApiStartup()
  }

  private def handleHttpApiStartup() = {

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
