package hmda.api

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.pipe
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ BaseHttpApi, HmdaCustomDirectives, InstitutionsHttpApi, LarHttpApi }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import HmdaConfig._

object HmdaFilingApi {
  def props(supervisor: ActorRef, querySupervisor: ActorRef, validationStats: ActorRef): Props =
    Props(new HmdaFilingApi(supervisor, querySupervisor, validationStats))
}

class HmdaFilingApi(supervisor: ActorRef, querySupervisor: ActorRef, validationStats: ActorRef)
    extends HttpApi
    with BaseHttpApi
    with LarHttpApi
    with InstitutionsHttpApi
    with HmdaCustomDirectives {

  implicit val flowParallelism = configuration.getInt("hmda.actor-flow-parallelism")

  override val name = "hmda-filing-api"

  lazy val httpTimeout = configuration.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  override lazy val host = configuration.getString("hmda.http.host")
  override lazy val port = configuration.getInt("hmda.http.port")

  implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val paths: Route = routes(s"$name") ~ institutionsRoutes(supervisor, querySupervisor, validationStats) ~ larRoutes(supervisor)

  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
