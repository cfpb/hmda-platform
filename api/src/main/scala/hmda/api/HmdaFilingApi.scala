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

object HmdaFilingApi {
  case object StartFilingApi
  def props(): Props = Props(new HmdaFilingApi)
}

class HmdaFilingApi
    extends HttpApi
    with BaseHttpApi
    with LarHttpApi
    with InstitutionsHttpApi
    with HmdaCustomDirectives {

  import HmdaFilingApi._

  val config = ConfigFactory.load()

  override val name = "hmda-filing-api"

  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  implicit val timeout = Timeout(httpTimeout.seconds)

  override lazy val host = config.getString("hmda.http.host")
  override lazy val port = config.getInt("hmda.http.port")

  implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val paths: Route = routes(s"$name") ~ larRoutes ~ institutionsRoutes

  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
