package hmda.api

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.pattern.{ ask, pipe }
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.http.public._
import hmda.api.http.BaseHttpApi
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import akka.http.scaladsl.server.Directives._
import hmda.persistence.institutions.InstitutionPersistence

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

object HmdaPublicApi {
  def props(supervisor: ActorRef): Props = Props(new HmdaPublicApi(supervisor))
}

class HmdaPublicApi(supervisor: ActorRef)
    extends HttpApi
    with BaseHttpApi
    with InstitutionSearchPaths
    with RateSpreadHttpApi
    with SingleLarValidationHttpApi
    with SingleTsValidationHttpApi
    with ULIHttpApi
    with HmdaFilerPaths {

  val config = ConfigFactory.load()
  override val parallelism = config.getInt("hmda.connectionFlowParallelism")

  lazy val httpTimeout = config.getInt("hmda.http.timeout")
  override implicit val timeout = Timeout(httpTimeout.seconds)

  override val name: String = "hmda-public-api"
  override val host: String = config.getString("hmda.http.publicHost")
  override val port: Int = config.getInt("hmda.http.publicPort")

  override implicit val system: ActorSystem = context.system
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = context.dispatcher
  override val log = Logging(system, getClass)

  val institutionPersistenceF = (supervisor ? FindActorByName(InstitutionPersistence.name))
    .mapTo[ActorRef]

  override val paths: Route =
    routes(s"$name") ~
      institutionSearchPath(institutionPersistenceF) ~
      uliHttpRoutes ~
      larRoutes(supervisor) ~
      tsRoutes(supervisor) ~
      rateSpreadRoutes(supervisor) ~
      hmdaFilersPath(supervisor) ~
      hmdaFilerMsasPath(supervisor)

  override val http: Future[ServerBinding] = Http(system).bindAndHandle(
    paths,
    host,
    port
  )

  http pipeTo self

}
